package com.souflow.models.services.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.souflow.config.MinioProperties;
import com.souflow.models.services.ImageService;

import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.Http.Method;
import io.minio.messages.Item;
import io.minio.ListObjectsArgs;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService { 

    private final MinioClient minioClient;
    
    private final MinioProperties props;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // Tối đa 10MB
    private static final java.util.Set<String> ALLOWED_EXTENSIONS = java.util.Set.of("jpg", "jpeg", "png", "webp", "gif", "avif", "svg");
    private static final java.util.Set<String> ALLOWED_MIME_TYPES = java.util.Set.of(
        "image/jpeg", "image/png", "image/webp", "image/gif", "image/avif", "image/svg+xml"
    );

    // Validate toàn diện file ảnh trước khi upload
    public static void validateImage(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File tải lên không được để trống.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Dung lượng ảnh vượt quá giới hạn cho phép (Tối đa 10MB).");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Tên file không hợp lệ.");
        }

        // 1. Kiểm tra Extension
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex + 1).toLowerCase().trim();
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Định dạng file không được hỗ trợ. Chỉ chấp nhận JPG, PNG, WEBP, GIF, AVIF, SVG.");
        }

        // 2. Kiểm tra MIME Content-Type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase().trim())) {
            throw new IllegalArgumentException("Loại file không hợp lệ (Content-Type không phải là hình ảnh).");
        }

        // 3. Kiểm tra Magic Bytes thực tế của file để chống đổi đuôi file giả mạo (.php -> .jpg)
        byte[] header = new byte[12];
        try (InputStream is = file.getInputStream()) {
            int bytesRead = is.read(header);
            if (bytesRead < 4) {
                throw new IllegalArgumentException("Nội dung file hình ảnh bị hỏng hoặc không hợp lệ.");
            }
            if (!isImageMagicBytes(header, extension)) {
                throw new IllegalArgumentException("Nội dung file không khớp với định dạng ảnh hợp lệ (Phát hiện file giả mạo).");
            }
        }
    }

    private static boolean isImageMagicBytes(byte[] header, String extension) {
        // JPEG: FF D8 FF
        if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
            return true;
        }
        // PNG: 89 50 4E 47
        if (header[0] == (byte) 0x89 && header[1] == (byte) 0x50 && header[2] == (byte) 0x4E && header[3] == (byte) 0x47) {
            return true;
        }
        // GIF: GIF8 (47 49 46 38)
        if (header[0] == (byte) 0x47 && header[1] == (byte) 0x49 && header[2] == (byte) 0x46 && header[3] == (byte) 0x38) {
            return true;
        }
        // WebP: RIFF....WEBP (52 49 46 46 .... 57 45 42 50)
        if (header[0] == (byte) 0x52 && header[1] == (byte) 0x49 && header[2] == (byte) 0x46 && header[3] == (byte) 0x46) {
            if (header.length >= 12 && header[8] == (byte) 0x57 && header[9] == (byte) 0x45 && header[10] == (byte) 0x42 && header[11] == (byte) 0x50) {
                return true;
            }
        }
        // SVG / XML
        if ("svg".equalsIgnoreCase(extension)) {
            String start = new String(header, 0, Math.min(header.length, 12)).trim().toLowerCase();
            return start.startsWith("<?xml") || start.startsWith("<svg");
        }
        // AVIF: ....ftyp (index 4-7)
        if ("avif".equalsIgnoreCase(extension)) {
            if (header.length >= 8 && header[4] == (byte) 0x66 && header[5] == (byte) 0x74 && header[6] == (byte) 0x79 && header[7] == (byte) 0x70) {
                return true;
            }
        }
        return false;
    }

    //save file with multipart
    public String upload(MultipartFile file) throws Exception {
        validateImage(file);
        
        // Làm sạch tên file gốc (Chống Path Traversal và ký tự đặc biệt)
        String rawName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
        String cleanName = rawName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String objectName = UUID.randomUUID() + "_" + cleanName;

        upload(file.getInputStream(), objectName, file.getContentType(), file.getSize());
        return objectName;
    }
    
    //helper for saving multipart
    public void upload(InputStream stream, String objectName, String contentType, long size) throws Exception {
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(props.bucket())
                .object(objectName)
                .stream(stream, size, -1L)
                .contentType(contentType)
                .build()
        );
    }

    //delete file
    public void delete(String objectName) throws Exception {
        minioClient.removeObject(
            RemoveObjectArgs.builder()
                .bucket(props.bucket())
                .object(objectName)
                .build()
        );
    }

    //read file from frontend
    public String getPresignedUrl(String objectName, Integer expiryDays) throws Exception {
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .bucket(props.bucket())
                .object(objectName)
                .method(Method.GET)
                .expiry(expiryDays, TimeUnit.DAYS)
                .build()
        );
    }

    @Override
    public String getPublicUrl(String objectName) throws Exception {
        if (objectName == null || objectName.isBlank()) {
            return null;
        }
        if (objectName.startsWith("http://") || objectName.startsWith("https://")) {
            return objectName;
        }
        return props.url() + "/" + props.bucket() + "/" + objectName;
    }

    //download file
    @Override
    public InputStream download(String objectName) throws Exception {
        return minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(props.bucket())
                .object(objectName)
                .build()
        );
    }

    //get all file names
    @Override
    public List<String> listObjects() throws Exception {
        List<String> names = new ArrayList<>();
        Iterable<Result<Item>> results = minioClient.listObjects(
            ListObjectsArgs.builder().bucket(props.bucket()).build()
        );
        for (Result<Item> result : results) {
            names.add(result.get().objectName());
        }
        return names;
    }
}
