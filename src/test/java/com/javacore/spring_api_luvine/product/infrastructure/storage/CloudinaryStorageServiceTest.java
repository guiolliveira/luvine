package com.javacore.spring_api_luvine.product.infrastructure.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.javacore.spring_api_luvine.product.application.dto.UploadResult;
import com.javacore.spring_api_luvine.product.domain.exception.FileStorageException;
import com.javacore.spring_api_luvine.product.domain.exception.InvalidFileUploadException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@DisplayName("CloudinaryStorageService")
@ExtendWith(MockitoExtension.class)
class CloudinaryStorageServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryStorageService cloudinaryStorageService;

    // --- HELPERS ------------------------------------------------------------------

    private static final String FOLDER = "products";
    private static final String FILE_NAME = "product-image";
    private static final String STORAGE_KEY = "products/product-image";
    private static final String IMAGE_URL =
            "https://res.cloudinary.com/demo/image/upload/product-image.jpg";

    private MultipartFile mockFile() {
        return mock(MultipartFile.class);
    }

    private Map<String, Object> uploadResponse() {
        return Map.of(
                "secure_url", IMAGE_URL,
                "public_id", STORAGE_KEY
        );
    }

    // --- UPLOAD -------------------------------------------------------------------

    @Nested
    @DisplayName("upload()")
    class Upload {

        @Test
        @DisplayName("deve retornar UploadResult quando upload realizado com sucesso")
        void upload_validFile_returnsUploadResult() throws Exception {
            MultipartFile file = mockFile();

            given(file.isEmpty()).willReturn(false);
            given(file.getBytes()).willReturn("image".getBytes());

            given(cloudinary.uploader()).willReturn(uploader);
            given(uploader.upload(any(byte[].class), anyMap()))
                    .willReturn(uploadResponse());

            UploadResult result =
                    cloudinaryStorageService.upload(file, FOLDER, FILE_NAME);

            assertThat(result).isNotNull();
            assertThat(result.imageUrl()).isEqualTo(IMAGE_URL);
            assertThat(result.storageKey()).isEqualTo(STORAGE_KEY);
        }

        @Test
        @DisplayName("deve lançar InvalidFileUploadException quando arquivo for nulo")
        void upload_nullFile_throwsInvalidFileUploadException() {
            assertThatExceptionOfType(InvalidFileUploadException.class)
                    .isThrownBy(() ->
                            cloudinaryStorageService.upload(
                                    null,
                                    FOLDER,
                                    FILE_NAME
                            ));
        }

        @Test
        @DisplayName("deve lançar InvalidFileUploadException quando arquivo estiver vazio")
        void upload_emptyFile_throwsInvalidFileUploadException() {
            MultipartFile file = mockFile();

            given(file.isEmpty()).willReturn(true);

            assertThatExceptionOfType(InvalidFileUploadException.class)
                    .isThrownBy(() ->
                            cloudinaryStorageService.upload(
                                    file,
                                    FOLDER,
                                    FILE_NAME
                            ));
        }

        @Test
        @DisplayName("deve lançar FileStorageException quando leitura do arquivo falhar")
        void upload_fileReadFails_throwsFileStorageException() throws Exception {
            MultipartFile file = mockFile();

            given(file.isEmpty()).willReturn(false);
            given(file.getBytes()).willThrow(new IOException());

            assertThatExceptionOfType(FileStorageException.class)
                    .isThrownBy(() ->
                            cloudinaryStorageService.upload(
                                    file,
                                    FOLDER,
                                    FILE_NAME
                            ));
        }

        @Test
        @DisplayName("deve lançar FileStorageException quando cloudinary falhar")
        void upload_cloudinaryFails_throwsFileStorageException() throws Exception {
            MultipartFile file = mockFile();

            given(file.isEmpty()).willReturn(false);
            given(file.getBytes()).willReturn("image".getBytes());

            given(cloudinary.uploader()).willReturn(uploader);

            given(uploader.upload(any(byte[].class), anyMap()))
                    .willThrow(new IOException());

            assertThatExceptionOfType(FileStorageException.class)
                    .isThrownBy(() ->
                            cloudinaryStorageService.upload(
                                    file,
                                    FOLDER,
                                    FILE_NAME
                            ));
        }

        @Test
        @DisplayName("deve enviar arquivo para cloudinary")
        void upload_validFile_callsCloudinaryUpload() throws Exception {
            MultipartFile file = mockFile();

            given(file.isEmpty()).willReturn(false);
            given(file.getBytes()).willReturn("image".getBytes());

            given(cloudinary.uploader()).willReturn(uploader);

            given(uploader.upload(any(byte[].class), anyMap()))
                    .willReturn(uploadResponse());

            cloudinaryStorageService.upload(file, FOLDER, FILE_NAME);

            then(uploader).should()
                    .upload(any(byte[].class), anyMap());
        }
    }

    // --- DELETE -------------------------------------------------------------------

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("deve deletar arquivo quando storageKey válida")
        void delete_validStorageKey_deletesFile() throws Exception {
            given(cloudinary.uploader()).willReturn(uploader);

            cloudinaryStorageService.delete(STORAGE_KEY);

            then(uploader).should()
                    .destroy(eq(STORAGE_KEY), anyMap());
        }

        @Test
        @DisplayName("não deve chamar cloudinary quando storageKey for nula")
        void delete_nullStorageKey_doesNotCallCloudinary() {
            cloudinaryStorageService.delete(null);

            then(cloudinary).should(never()).uploader();
        }

        @Test
        @DisplayName("não deve chamar cloudinary quando storageKey estiver vazia")
        void delete_blankStorageKey_doesNotCallCloudinary() {
            cloudinaryStorageService.delete(" ");

            then(cloudinary).should(never()).uploader();
        }

        @Test
        @DisplayName("deve lançar FileStorageException quando cloudinary falhar ao deletar")
        void delete_cloudinaryFails_throwsFileStorageException() throws Exception {
            given(cloudinary.uploader()).willReturn(uploader);

            given(uploader.destroy(anyString(), anyMap()))
                    .willThrow(new IOException());

            assertThatExceptionOfType(FileStorageException.class)
                    .isThrownBy(() ->
                            cloudinaryStorageService.delete(STORAGE_KEY));
        }
    }
}