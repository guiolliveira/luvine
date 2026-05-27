package com.javacore.spring_api_luvine.product.infrastructure.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String upload(MultipartFile file, String folder, String fileName);

    void delete(String storageKey);
}