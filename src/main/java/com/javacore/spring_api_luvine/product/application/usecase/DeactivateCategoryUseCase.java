package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryNotFoundException;
import com.javacore.spring_api_luvine.product.infrastructure.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@UseCase
@RequiredArgsConstructor
public class DeactivateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    @Transactional
    public void execute(UUID categoryPublicId) {
        Category category = categoryRepository.findByPublicId(categoryPublicId)
                .orElseThrow(CategoryNotFoundException::new);

        category.deactivate();
    }
}