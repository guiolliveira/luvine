package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryNotFoundException;
import com.javacore.spring_api_luvine.product.infrastructure.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class ActivateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    @Transactional
    public void execute(UUID categoryPublicId) {
        log.info("event=activate_category_attempt publicId={}", categoryPublicId);

        Category category = categoryRepository.findByPublicId(categoryPublicId)
                .orElseThrow(() -> {
                    log.warn("event=activate_category_rejected reason=category_not_found publicId={}",
                            categoryPublicId);
                    return new CategoryNotFoundException();
                });

        category.activate();

        log.info("event=activate_category_completed publicId={}", categoryPublicId);
    }
}