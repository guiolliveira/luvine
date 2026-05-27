package com.javacore.spring_api_luvine.product.infrastructure.storage;

import com.javacore.spring_api_luvine.product.application.dto.UploadResult;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    UploadResult upload(MultipartFile file, String folder, String fileName);

    void delete(String storageKey);
}