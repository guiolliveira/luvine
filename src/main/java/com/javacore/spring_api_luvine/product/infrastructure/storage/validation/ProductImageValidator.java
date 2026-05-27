package com.javacore.spring_api_luvine.product.infrastructure.storage.validation;

import com.javacore.spring_api_luvine.product.domain.exception.InvalidFileUploadException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Component
public class ProductImageValidator {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    public void validate(MultipartFile file) {
        validateEmptyFile(file);
        validateFileSize(file);
        validateContentType(file);
    }

    private void validateEmptyFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileUploadException("Arquivo não encontrado");
        }
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileUploadException("Esse arquivo excede o tamanho máximo permitido de 5MB");
        }
    }

    private void validateContentType(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileUploadException("Formato do arquivo é inválido");
        }
    }
}