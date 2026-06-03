package com.javacore.spring_api_luvine.product.infrastructure.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.javacore.spring_api_luvine.product.application.dto.UploadResult;
import com.javacore.spring_api_luvine.product.domain.exception.FileStorageException;
import com.javacore.spring_api_luvine.product.domain.exception.InvalidFileUploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryStorageService implements StorageService {

    private final Cloudinary cloudinary;

    @Override
    public UploadResult upload(MultipartFile file, String folder, String fileName) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileUploadException("Arquivo não encontrado");
        }

        log.info("event=storage_upload_attempt folder={} fileName={}", folder, fileName);

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "public_id", fileName,
                            "overwrite", true,
                            "resource_type", "image"
                    )
            );

            UploadResult uploadResult = new UploadResult(
                    (String) result.get("secure_url"),
                    (String) result.get("public_id")
            );

            log.info("event=storage_upload_completed folder={} fileName={}", folder, fileName);
            return uploadResult;
        } catch (IOException ex) {
            log.error("event=storage_upload_error folder={} fileName={}", folder, fileName, ex);
            throw new FileStorageException();
        }
    }

    @Override
    public void delete(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) return;

        log.info("event=storage_delete_attempt storageKey={}", storageKey);

        try {
            cloudinary.uploader().destroy(
                    storageKey,
                    ObjectUtils.asMap("invalidate", true)
            );

            log.info("event=storage_delete_completed storageKey={}", storageKey);
        } catch (IOException ex) {
            log.error("event=storage_delete_error storageKey={}", storageKey, ex);
            throw new FileStorageException();
        }
    }
}