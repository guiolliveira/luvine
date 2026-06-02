package com.javacore.spring_api_luvine.product.infrastructure.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.javacore.spring_api_luvine.product.application.dto.UploadResult;
import com.javacore.spring_api_luvine.product.domain.exception.FileStorageException;
import com.javacore.spring_api_luvine.product.domain.exception.InvalidFileUploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryStorageService implements StorageService {

    private final Cloudinary cloudinary;

    @Override
    public UploadResult upload(MultipartFile file, String folder, String fileName) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileUploadException("Arquivo não encontrado");
        }

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

            return new UploadResult(
                    (String) result.get("secure_url"),
                    (String) result.get("public_id")
            );
        } catch (IOException ex) {
            throw new FileStorageException();
        }
    }

    @Override
    public void delete(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) return;

        try {
            cloudinary.uploader().destroy(
                    storageKey,
                    ObjectUtils.asMap("invalidate", true)
            );
        } catch (IOException ex) {
            throw new FileStorageException();
        }
    }
}