package com.javacore.spring_api_luvine.product.infrastructure.storage.validation;

import com.javacore.spring_api_luvine.product.domain.exception.InvalidFileUploadException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("FileImageValidator")
class FileImageValidatorTest {

    private final FileImageValidator fileImageValidator = new FileImageValidator();

    // --- HELPERS ------------------------------------------------------------------

    private MultipartFile mockFile() {
        return mock(MultipartFile.class);
    }

    // --- VALIDATE -----------------------------------------------------------------

    @Nested
    @DisplayName("validate()")
    class Validate {

        @Test
        @DisplayName("deve validar arquivo jpeg válido")
        void validate_validJpegFile_doesNotThrowException() {
            MultipartFile file = mockFile();

            given(file.isEmpty()).willReturn(false);
            given(file.getSize()).willReturn(1024L);
            given(file.getContentType()).willReturn("image/jpeg");

            assertThatCode(() -> fileImageValidator.validate(file))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("deve validar arquivo png válido")
        void validate_validPngFile_doesNotThrowException() {
            MultipartFile file = mockFile();

            given(file.isEmpty()).willReturn(false);
            given(file.getSize()).willReturn(1024L);
            given(file.getContentType()).willReturn("image/png");

            assertThatCode(() -> fileImageValidator.validate(file))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("deve validar arquivo webp válido")
        void validate_validWebpFile_doesNotThrowException() {
            MultipartFile file = mockFile();

            given(file.isEmpty()).willReturn(false);
            given(file.getSize()).willReturn(1024L);
            given(file.getContentType()).willReturn("image/webp");

            assertThatCode(() -> fileImageValidator.validate(file))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("deve validar content type em maiúsculo")
        void validate_upperCaseContentType_doesNotThrowException() {
            MultipartFile file = mockFile();

            given(file.isEmpty()).willReturn(false);
            given(file.getSize()).willReturn(1024L);
            given(file.getContentType()).willReturn("IMAGE/JPEG");

            assertThatCode(() -> fileImageValidator.validate(file))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("deve lançar InvalidFileUploadException quando arquivo for nulo")
        void validate_nullFile_throwsInvalidFileUploadException() {
            assertThatExceptionOfType(InvalidFileUploadException.class)
                    .isThrownBy(() -> fileImageValidator.validate(null));
        }

        @Test
        @DisplayName("deve lançar InvalidFileUploadException quando arquivo estiver vazio")
        void validate_emptyFile_throwsInvalidFileUploadException() {
            MultipartFile file = mockFile();

            given(file.isEmpty()).willReturn(true);

            assertThatExceptionOfType(InvalidFileUploadException.class)
                    .isThrownBy(() -> fileImageValidator.validate(file));
        }

        @Test
        @DisplayName("deve lançar InvalidFileUploadException quando arquivo exceder 5MB")
        void validate_fileExceedsMaxSize_throwsInvalidFileUploadException() {
            MultipartFile file = mockFile();

            given(file.isEmpty()).willReturn(false);
            given(file.getSize()).willReturn((5 * 1024 * 1024) + 1L);
            given(file.getContentType()).willReturn("image/jpeg");

            assertThatExceptionOfType(InvalidFileUploadException.class)
                    .isThrownBy(() -> fileImageValidator.validate(file));
        }

        @Test
        @DisplayName("deve lançar InvalidFileUploadException quando content type for nulo")
        void validate_nullContentType_throwsInvalidFileUploadException() {
            MultipartFile file = mockFile();

            given(file.isEmpty()).willReturn(false);
            given(file.getSize()).willReturn(1024L);
            given(file.getContentType()).willReturn(null);

            assertThatExceptionOfType(InvalidFileUploadException.class)
                    .isThrownBy(() -> fileImageValidator.validate(file));
        }

        @Test
        @DisplayName("deve lançar InvalidFileUploadException quando content type for inválido")
        void validate_invalidContentType_throwsInvalidFileUploadException() {
            MultipartFile file = mockFile();

            given(file.isEmpty()).willReturn(false);
            given(file.getSize()).willReturn(1024L);
            given(file.getContentType()).willReturn("application/pdf");

            assertThatExceptionOfType(InvalidFileUploadException.class)
                    .isThrownBy(() -> fileImageValidator.validate(file));
        }
    }
}