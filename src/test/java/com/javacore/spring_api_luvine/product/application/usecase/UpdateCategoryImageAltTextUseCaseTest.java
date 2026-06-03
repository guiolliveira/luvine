package com.javacore.spring_api_luvine.product.application.usecase;

import com.javacore.spring_api_luvine.common.exception.exceptions.UnchangedValueException;
import com.javacore.spring_api_luvine.product.application.dto.CategoryImageResponse;
import com.javacore.spring_api_luvine.product.application.dto.UpdateImageAltTextRequest;
import com.javacore.spring_api_luvine.product.application.mapper.CategoryMapper;
import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.domain.entity.CategoryImage;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryImageNotFoundException;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryNotFoundException;
import com.javacore.spring_api_luvine.product.domain.valueObject.AltText;
import com.javacore.spring_api_luvine.product.infrastructure.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@DisplayName("UpdateCategoryImageAltTextUseCase")
@ExtendWith(MockitoExtension.class)
class UpdateCategoryImageAltTextUseCaseTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper categoryMapper;

    @InjectMocks
    private UpdateCategoryImageAltTextUseCase updateCategoryImageAltTextUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID CATEGORY_ID = UUID.randomUUID();
    private static final UUID IMAGE_ID = UUID.randomUUID();
    private static final String NEW_ALT_TEXT = "Novo texto alternativo da imagem";

    private UpdateImageAltTextRequest validRequest() {
        return new UpdateImageAltTextRequest(NEW_ALT_TEXT);
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve alterar altText e retornar CategoryImageResponse")
        void execute_validRequest_returnsCategoryImageResponse() {
            Category category = mock(Category.class);
            CategoryImage image = mock(CategoryImage.class);
            CategoryImageResponse response = mock(CategoryImageResponse.class);

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(category.findImageByPublicId(IMAGE_ID)).willReturn(image);
            given(categoryMapper.toCategoryImageResponse(image)).willReturn(response);

            CategoryImageResponse result = updateCategoryImageAltTextUseCase.execute(
                    CATEGORY_ID, IMAGE_ID, validRequest());

            assertThat(result).isEqualTo(response);
            then(image).should().changeAltText(new AltText(NEW_ALT_TEXT));
        }

        @Test
        @DisplayName("deve lançar CategoryNotFoundException quando categoria não encontrada")
        void execute_categoryNotFound_throwsCategoryNotFoundException() {
            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(CategoryNotFoundException.class)
                    .isThrownBy(() -> updateCategoryImageAltTextUseCase.execute(
                            CATEGORY_ID, IMAGE_ID, validRequest()));

            then(categoryMapper).should(never()).toCategoryImageResponse(any());
        }

        @Test
        @DisplayName("deve lançar CategoryImageNotFoundException quando imagem não encontrada")
        void execute_imageNotFound_throwsCategoryImageNotFoundException() {
            Category category = mock(Category.class);

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(category.findImageByPublicId(IMAGE_ID)).willThrow(new CategoryImageNotFoundException());

            assertThatExceptionOfType(CategoryImageNotFoundException.class)
                    .isThrownBy(() -> updateCategoryImageAltTextUseCase.execute(
                            CATEGORY_ID, IMAGE_ID, validRequest()));

            then(categoryMapper).should(never()).toCategoryImageResponse(any());
        }

        @Test
        @DisplayName("deve propagar UnchangedValueException quando altText é igual ao atual")
        void execute_sameAltText_propagatesUnchangedValueException() {
            Category category = mock(Category.class);
            CategoryImage image = mock(CategoryImage.class);

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(category.findImageByPublicId(IMAGE_ID)).willReturn(image);
            doThrow(new UnchangedValueException("A imagem já possui esse texto alternativo"))
                    .when(image).changeAltText(any(AltText.class));

            assertThatExceptionOfType(UnchangedValueException.class)
                    .isThrownBy(() -> updateCategoryImageAltTextUseCase.execute(
                            CATEGORY_ID, IMAGE_ID, validRequest()));

            then(categoryMapper).should(never()).toCategoryImageResponse(any());
        }

        @Test
        @DisplayName("deve chamar changeAltText com o AltText correto")
        void execute_validRequest_callsChangeAltTextWithCorrectValue() {
            Category category = mock(Category.class);
            CategoryImage image = mock(CategoryImage.class);

            given(categoryRepository.findByPublicId(CATEGORY_ID)).willReturn(Optional.of(category));
            given(category.findImageByPublicId(IMAGE_ID)).willReturn(image);
            given(categoryMapper.toCategoryImageResponse(image)).willReturn(mock(CategoryImageResponse.class));

            updateCategoryImageAltTextUseCase.execute(CATEGORY_ID, IMAGE_ID, validRequest());

            then(image).should().changeAltText(new AltText(NEW_ALT_TEXT));
        }
    }
}