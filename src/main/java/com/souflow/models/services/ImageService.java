package com.souflow.models.services;

import java.io.InputStream;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    String upload(MultipartFile file) throws Exception;

    void delete(String objectName) throws Exception;

    InputStream download(String objectName) throws Exception; 

    String getPublicUrl(String objectName) throws Exception;

    List<String> listObjects() throws Exception;

}
