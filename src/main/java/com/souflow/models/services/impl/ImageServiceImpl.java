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

    //save file with multipart
    public String upload(MultipartFile file) throws Exception {
        String objectName = UUID.randomUUID() + "_" + file.getOriginalFilename();
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
