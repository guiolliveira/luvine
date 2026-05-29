package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.application.dto.CategoryImageResponse;
import com.javacore.spring_api_luvine.product.application.dto.CreateCategoryImageRequest;
import com.javacore.spring_api_luvine.product.application.dto.UploadResult;
import com.javacore.spring_api_luvine.product.application.mapper.CategoryMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.domain.entity.CategoryImage;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryNotFoundException;
import com.javacore.spring_api_luvine.product.domain.valueObject.AltText;
import com.javacore.spring_api_luvine.product.infrastructure.repository.CategoryRepository;
import com.javacore.spring_api_luvine.product.infrastructure.storage.StorageService;
import com.javacore.spring_api_luvine.product.infrastructure.storage.validation.FileImageValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class CreateCategoryImageUseCase {

    private final CategoryRepository categoryRepository;
    private final StorageService storageService;
    private final FileImageValidator imageValidator;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryImageResponse execute(
            UUID categoryPublicId, MultipartFile file, CreateCategoryImageRequest request) {

        imageValidator.validate(file);

        Category category = categoryRepository.findByPublicId(categoryPublicId)
                .orElseThrow(CategoryNotFoundException::new);

        CategoryImage oldImage = category.getImage();

        String folder = String.format("categories/%s/images", category.getPublicId());
        UUID imagePublicId = UUID.randomUUID();

        UploadResult uploadResult = null;

        try {
            uploadResult = storageService.upload(file, folder, imagePublicId.toString());

            CategoryImage categoryImage = CategoryImage.create(
                    uploadResult.imageUrl(),
                    uploadResult.storageKey(),
                    new AltText(request.altText())
            );

            category.addCategoryImage(categoryImage);

            if (oldImage != null) {
                storageService.delete(uploadResult.storageKey());
            }

            return categoryMapper.toCategoryImageResponse(categoryImage);
        } catch (Exception ex) {
            if (uploadResult != null) {
                try {
                    storageService.delete(uploadResult.storageKey());
                } catch (Exception rollbackEx) {
                    log.error(
                            "IMAGE_ROLLBACK_FAILED storageKey={}, categoryPublicId={}",
                            uploadResult.storageKey(),
                            category.getPublicId(),
                            rollbackEx
                    );
                }
            }
            throw ex;
        }
    }
}