package com.javacore.spring_api_luvine.shared.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@DisplayName("GlobalExceptionHandler")
@WebMvcTest(controllers = GlobalExceptionHandlerTest.FakeController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.FakeServiceConfig.class})
class GlobalExceptionHandlerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    FakeController.FakeService fakeService;

    // --- INFRAESTRUTURA DE TESTE --------------------------------------------------------------

    @TestConfiguration
    static class FakeServiceConfig {

        // Instância compartilhada entre o Spring e os métodos de teste
        static final FakeController.FakeService INSTANCE =
                mock(FakeController.FakeService.class);

        @Bean
        FakeController.FakeService fakeService() {
            return INSTANCE;
        }
    }

    @RestController
    @RequestMapping("/fake")
    static class FakeController {

        private final FakeService fakeService;

        FakeController(FakeService fakeService) {
            this.fakeService = fakeService;
        }

        @GetMapping("/business")
        public void business() {
            fakeService.throwBusiness();
        }

        @PostMapping("/validation")
        public void validation(@RequestBody @jakarta.validation.Valid FakeDTO dto) { }

        @GetMapping("/type-mismatch")
        public void typeMismatch(@RequestParam Integer id) { }

        @GetMapping("/integrity")
        public void integrity() {
            fakeService.throwIntegrity();
        }

        @PostMapping("/refresh")
        public void missingCookie(@CookieValue("refreshToken") String token) { }

        @GetMapping("/auth")
        public void authentication() {
            fakeService.throwAuthentication();
        }

        @GetMapping("/generic")
        public void generic() {
            fakeService.throwGeneric();
        }

        record FakeDTO(
                @jakarta.validation.constraints.NotBlank(message = "Campo é obrigatório")
                String campo
        ) { }

        interface FakeService {
            void throwBusiness();
            void throwIntegrity();
            void throwAuthentication();
            void throwGeneric();
        }
    }

    // --- BUSINESS EXCEPTION --------------------------------------------------------------

    @Nested
    @DisplayName("handleBusinessException()")
    class HandleBusinessException {

        /**
         * Cria uma {@link BusinessException} anônima com o {@link ErrorCode} informado
         * para isolar o teste da existência de subclasses concretas.
         */
        private BusinessException businessExceptionWith(ErrorCode code) {
            return new BusinessException("Erro de teste", code) { };
        }

        @Test
        @DisplayName("deve retornar o status HTTP definido no ErrorCode")
        void shouldReturnStatusDefinedInErrorCode() throws Exception {
            ErrorCode anyCode = ErrorCode.values()[0];
            doThrow(businessExceptionWith(anyCode)).when(fakeService).throwBusiness();

            mockMvc.perform(get("/fake/business"))
                    .andExpect(status().is(anyCode.getStatus().value()));
        }

        @Test
        @DisplayName("deve retornar o nome do ErrorCode no campo errorCode")
        void shouldReturnErrorCodeName() throws Exception {
            ErrorCode anyCode = ErrorCode.values()[0];
            doThrow(businessExceptionWith(anyCode)).when(fakeService).throwBusiness();

            mockMvc.perform(get("/fake/business"))
                    .andExpect(jsonPath("$.errorCode").value(anyCode.name()));
        }

        @Test
        @DisplayName("deve retornar a mensagem da exceção no corpo da resposta")
        void shouldReturnExceptionMessageInBody() throws Exception {
            ErrorCode anyCode = ErrorCode.values()[0];
            BusinessException ex = businessExceptionWith(anyCode);
            doThrow(ex).when(fakeService).throwBusiness();

            mockMvc.perform(get("/fake/business"))
                    .andExpect(jsonPath("$.message").value(ex.getMessage()));
        }

        @Test
        @DisplayName("deve preencher o campo path com o URI da requisição")
        void shouldIncludeRequestUriInPath() throws Exception {
            doThrow(businessExceptionWith(ErrorCode.values()[0])).when(fakeService).throwBusiness();

            mockMvc.perform(get("/fake/business"))
                    .andExpect(jsonPath("$.path").value("/fake/business"));
        }

        @Test
        @DisplayName("deve incluir status numérico e campo error no corpo")
        void shouldIncludeNumericStatusAndErrorField() throws Exception {
            ErrorCode anyCode = ErrorCode.values()[0];
            doThrow(businessExceptionWith(anyCode)).when(fakeService).throwBusiness();

            mockMvc.perform(get("/fake/business"))
                    .andExpect(jsonPath("$.status").value(anyCode.getStatus().value()))
                    .andExpect(jsonPath("$.error").isNotEmpty());
        }
    }

    // --- METHOD ARGUMENT NOT VALID EXCEPTION --------------------------------------------------------------

    @Nested
    @DisplayName("handleNotValidException()")
    class HandleNotValidException {

        @Test
        @DisplayName("deve retornar 400 quando payload inválido for enviado")
        void shouldReturn400ForInvalidPayload() throws Exception {
            mockMvc.perform(post("/fake/validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "campo": "" }
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve retornar errorCode ARGUMENT_NOT_VALID")
        void shouldReturnArgumentNotValidErrorCode() throws Exception {
            mockMvc.perform(post("/fake/validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "campo": "" }
                                    """))
                    .andExpect(jsonPath("$.errorCode").value("ARGUMENT_NOT_VALID"));
        }

        @Test
        @DisplayName("deve retornar lista de erros de campo em details")
        void shouldReturnFieldErrorsInDetails() throws Exception {
            mockMvc.perform(post("/fake/validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "campo": "" }
                                    """))
                    .andExpect(jsonPath("$.details").isArray())
                    .andExpect(jsonPath("$.details[0]").value("Campo é obrigatório"));
        }

        @Test
        @DisplayName("deve retornar a mensagem padrão 'Argumento Inválido'")
        void shouldReturnDefaultInvalidArgumentMessage() throws Exception {
            mockMvc.perform(post("/fake/validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "campo": "" }
                                    """))
                    .andExpect(jsonPath("$.message").value("Argumento Inválido"))
                    .andExpect(jsonPath("$.path").value("/fake/validation"));
        }

        @Test
        @DisplayName("deve retornar 200 quando payload for válido")
        void shouldReturn200WhenPayloadIsValid() throws Exception {
            mockMvc.perform(post("/fake/validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "campo": "valor valido" }
                                    """))
                    .andExpect(status().isOk());
        }
    }

    // --- METHOD ARGUMENT TYPE MISMATCH EXCEPTION --------------------------------------------------------------

    @Nested
    @DisplayName("handleTypeMisMatchException()")
    class HandleTypeMisMatchException {

        @Test
        @DisplayName("deve retornar 400 quando parâmetro tiver tipo inválido")
        void shouldReturn400WhenParameterTypeIsInvalid() throws Exception {
            mockMvc.perform(get("/fake/type-mismatch").param("id", "nao-e-numero"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve retornar errorCode ARGUMENT_TYPE_MISMATCH")
        void shouldReturnArgumentTypeMismatchErrorCode() throws Exception {
            mockMvc.perform(get("/fake/type-mismatch").param("id", "abc"))
                    .andExpect(jsonPath("$.errorCode").value("ARGUMENT_TYPE_MISMATCH"));
        }

        @Test
        @DisplayName("deve retornar a mensagem 'Parâmetro Inválido'")
        void shouldReturnInvalidParameterMessage() throws Exception {
            mockMvc.perform(get("/fake/type-mismatch").param("id", "abc"))
                    .andExpect(jsonPath("$.message").value("Parâmetro Inválido"));
        }

        @Test
        @DisplayName("deve incluir path correto para type mismatch")
        void shouldIncludeCorrectPathForTypeMismatch() throws Exception {
            mockMvc.perform(get("/fake/type-mismatch").param("id", "abc"))
                    .andExpect(jsonPath("$.path").value("/fake/type-mismatch"));
        }
    }

    // --- DATA INTEGRITY VIOLATION EXCEPTION --------------------------------------------------------------

    @Nested
    @DisplayName("handleIntegrityViolationException()")
    class HandleIntegrityViolationException {

        @Test
        @DisplayName("deve retornar 400 para violação de integridade de dados")
        void shouldReturn400ForDataIntegrityViolation() throws Exception {
            doThrow(new DataIntegrityViolationException("constraint violation"))
                    .when(fakeService).throwIntegrity();

            mockMvc.perform(get("/fake/integrity"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve retornar errorCode INTEGRITY_VIOLATION")
        void shouldReturnIntegrityViolationErrorCode() throws Exception {
            doThrow(new DataIntegrityViolationException("constraint"))
                    .when(fakeService).throwIntegrity();

            mockMvc.perform(get("/fake/integrity"))
                    .andExpect(jsonPath("$.errorCode").value("INTEGRITY_VIOLATION"));
        }

        @Test
        @DisplayName("deve retornar a mensagem 'Erro de Violação'")
        void shouldReturnViolationErrorMessage() throws Exception {
            doThrow(new DataIntegrityViolationException("constraint"))
                    .when(fakeService).throwIntegrity();

            mockMvc.perform(get("/fake/integrity"))
                    .andExpect(jsonPath("$.message").value("Erro de Violação"));
        }

        @Test
        @DisplayName("deve incluir path correto para violação de integridade")
        void shouldIncludeCorrectPathForIntegrityViolation() throws Exception {
            doThrow(new DataIntegrityViolationException("constraint"))
                    .when(fakeService).throwIntegrity();

            mockMvc.perform(get("/fake/integrity"))
                    .andExpect(jsonPath("$.path").value("/fake/integrity"));
        }
    }

    // --- MISSING REQUEST COOKIE EXCEPTION --------------------------------------------------------------

    @Nested
    @DisplayName("handleRequestCookieException()")
    class HandleRequestCookieException {

        @Test
        @DisplayName("deve retornar 400 quando cookie obrigatório estiver ausente")
        void shouldReturn400WhenRequiredCookieIsMissing() throws Exception {
            mockMvc.perform(post("/fake/refresh"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve retornar errorCode MISSING_REQUEST_COOKIE")
        void shouldReturnMissingRequestCookieErrorCode() throws Exception {
            mockMvc.perform(post("/fake/refresh"))
                    .andExpect(jsonPath("$.errorCode").value("MISSING_REQUEST_COOKIE"));
        }

        @Test
        @DisplayName("deve retornar a mensagem 'O cookie é obrigatório'")
        void shouldReturnCookieRequiredMessage() throws Exception {
            mockMvc.perform(post("/fake/refresh"))
                    .andExpect(jsonPath("$.message").value("O cookie é obrigatório"));
        }

        @Test
        @DisplayName("deve incluir path correto para cookie ausente")
        void shouldIncludeCorrectPathForMissingCookie() throws Exception {
            mockMvc.perform(post("/fake/refresh"))
                    .andExpect(jsonPath("$.path").value("/fake/refresh"));
        }
    }

    // --- AUTHENTICATION EXCEPTION --------------------------------------------------------------

    @Nested
    @DisplayName("handleAuthenticationException()")
    class HandleAuthenticationException {

        @Test
        @DisplayName("deve retornar 401 para AuthenticationException")
        void shouldReturn401ForAuthenticationException() throws Exception {
            doThrow(new AuthenticationException("credenciais ruins") { })
                    .when(fakeService).throwAuthentication();

            mockMvc.perform(get("/fake/auth"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("deve retornar errorCode AUTHENTICATION_UNAUTHORIZED")
        void shouldReturnAuthenticationUnauthorizedErrorCode() throws Exception {
            doThrow(new AuthenticationException("credenciais ruins") { })
                    .when(fakeService).throwAuthentication();

            mockMvc.perform(get("/fake/auth"))
                    .andExpect(jsonPath("$.errorCode").value("AUTHENTICATION_UNAUTHORIZED"));
        }

        @Test
        @DisplayName("deve retornar a mensagem fixa 'Credenciais inválidas'")
        void shouldReturnFixedInvalidCredentialsMessage() throws Exception {
            doThrow(new AuthenticationException("mensagem interna sensível") { })
                    .when(fakeService).throwAuthentication();

            mockMvc.perform(get("/fake/auth"))
                    .andExpect(jsonPath("$.message").value("Credenciais inválidas"));
        }

        @Test
        @DisplayName("deve incluir path correto para falha de autenticação")
        void shouldIncludeCorrectPathForAuthenticationFailure() throws Exception {
            doThrow(new AuthenticationException("sessão expirada") { })
                    .when(fakeService).throwAuthentication();

            mockMvc.perform(get("/fake/auth"))
                    .andExpect(jsonPath("$.path").value("/fake/auth"));
        }
    }

    // --- GENERIC EXCEPTION --------------------------------------------------------------

    @Nested
    @DisplayName("handleGenericException()")
    class HandleGenericException {

        @Test
        @DisplayName("deve retornar 500 para exceção não tratada")
        void shouldReturn500ForUnhandledException() throws Exception {
            doThrow(new RuntimeException("erro inesperado"))
                    .when(fakeService).throwGeneric();

            mockMvc.perform(get("/fake/generic"))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("deve retornar errorCode INTERNAL_SERVER_ERROR")
        void shouldReturnInternalServerErrorCode() throws Exception {
            doThrow(new RuntimeException("boom"))
                    .when(fakeService).throwGeneric();

            mockMvc.perform(get("/fake/generic"))
                    .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"));
        }

        @Test
        @DisplayName("deve retornar a mensagem 'Erro Inesperado' sem expor detalhes internos")
        void shouldReturnGenericMessageWithoutInternalDetails() throws Exception {
            // Detalhes da exceção não devem vazar para o cliente
            doThrow(new RuntimeException("stack trace sensível aqui"))
                    .when(fakeService).throwGeneric();

            mockMvc.perform(get("/fake/generic"))
                    .andExpect(jsonPath("$.message").value("Erro Inesperado"));
        }

        @Test
        @DisplayName("deve incluir path correto para exceção genérica")
        void shouldIncludeCorrectPathForGenericException() throws Exception {
            doThrow(new RuntimeException("qualquer erro"))
                    .when(fakeService).throwGeneric();

            mockMvc.perform(get("/fake/generic"))
                    .andExpect(jsonPath("$.path").value("/fake/generic"));
        }
    }

    // --- API ERROR --------------------------------------------------------------

    @Nested
    @DisplayName("Contrato ApiError — campos obrigatórios")
    class ApiErrorContract {

        @Test
        @DisplayName("toda resposta de erro deve conter os cinco campos obrigatórios")
        void errorResponseShouldAlwaysContainMandatoryFields() throws Exception {
            doThrow(new RuntimeException("erro"))
                    .when(fakeService).throwGeneric();

            mockMvc.perform(get("/fake/generic"))
                    .andExpect(jsonPath("$.status").isNumber())
                    .andExpect(jsonPath("$.error").isNotEmpty())
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.errorCode").isNotEmpty())
                    .andExpect(jsonPath("$.path").isNotEmpty());
        }

        @Test
        @DisplayName("campo details não deve conter erros de campo quando não há validação")
        void detailsShouldBeNullOrAbsentWhenNoFieldErrors() throws Exception {
            // O handler passa details=null. Dependendo de @JsonInclude no ApiError,
            // o campo pode estar ausente, null, ou serializado como [].
            // Para garantir ausência total: adicione @JsonInclude(NON_NULL) no ApiError.
            // Este teste aceita as três formas toleradas.
            doThrow(new RuntimeException("erro"))
                    .when(fakeService).throwGeneric();

            mockMvc.perform(get("/fake/generic"))
                    .andExpect(result -> {
                        String body = result.getResponse().getContentAsString();
                        boolean absent   = !body.contains("\"details\"");
                        boolean nullVal  = body.contains("\"details\":null");
                        boolean emptyArr = body.contains("\"details\":[]");
                        if (!absent && !nullVal && !emptyArr) {
                            throw new AssertionError(
                                    "Campo \'details\' deveria ser null, ausente ou [] mas foi: " + body);
                        }
                    });
        }

        @Test
        @DisplayName("campo details deve estar presente e ser array na exceção de validação")
        void detailsShouldBePresentAndBeArrayForValidationException() throws Exception {
            mockMvc.perform(post("/fake/validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "campo": "" }
                                    """))
                    .andExpect(jsonPath("$.details").isArray());
        }

        @Test
        @DisplayName("campo status deve coincidir com o HTTP status code da resposta")
        void statusFieldShouldMatchHttpResponseStatus() throws Exception {
            doThrow(new RuntimeException("erro"))
                    .when(fakeService).throwGeneric();

            mockMvc.perform(get("/fake/generic"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500));
        }

        @Test
        @DisplayName("Content-Type da resposta deve ser application/json")
        void responseContentTypeShouldBeJson() throws Exception {
            doThrow(new RuntimeException("erro"))
                    .when(fakeService).throwGeneric();

            mockMvc.perform(get("/fake/generic"))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        }
    }
}