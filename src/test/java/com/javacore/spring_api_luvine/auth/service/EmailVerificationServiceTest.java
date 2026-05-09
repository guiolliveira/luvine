package com.javacore.spring_api_luvine.auth.service;

import com.javacore.spring_api_luvine.auth.domain.entity.EmailVerification;
import com.javacore.spring_api_luvine.auth.domain.exception.EmailAlreadyVerifiedException;
import com.javacore.spring_api_luvine.auth.domain.exception.InvalidCodeException;
import com.javacore.spring_api_luvine.auth.domain.exception.RateLimitExceededException;
import com.javacore.spring_api_luvine.auth.dto.EmailVerificationCreationResult;
import com.javacore.spring_api_luvine.auth.repository.EmailVerificationRepository;
import com.javacore.spring_api_luvine.shared.limiter.service.RateLimiterService;
import com.javacore.spring_api_luvine.shared.util.GenerateCode;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.repository.UserRepository;
import io.github.bucket4j.ConsumptionProbe;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("EmailVerificationService")
class EmailVerificationServiceTest {

    @Mock private EmailVerificationRepository verificationRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RateLimiterService rateLimiterService;
    @Mock private GenerateCode generateCode;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    private static final String RAW_CODE    = "123456";

    private ConsumptionProbe probeConsumed() {
        return ConsumptionProbe.consumed(1, 9);
    }

    private ConsumptionProbe probeExhausted() {
        return ConsumptionProbe.rejected(0, 1L, 1L);
    }

    // --- CREATE CODE ---------------------------------------------------------------

    @Nested
    @DisplayName("createCode()")
    class CreateCode {

        @Test
        @DisplayName("deve criar código com sucesso para usuário sem envios anteriores")
        void createCode_firstRequest_returnsCreationResult() {
            User user = mock(User.class);
            given(user.getPublicId()).willReturn(UUID.randomUUID());
            given(user.getId()).willReturn(1L);
            given(user.getLastVerificationEmailSentAt()).willReturn(null);
            given(user.getVerificationEmailRequestCount()).willReturn(0);
            given(rateLimiterService.tryConsume(any())).willReturn(probeConsumed());
            given(generateCode.generate()).willReturn(RAW_CODE);
            given(passwordEncoder.encode(RAW_CODE)).willReturn("encoded-code");
            given(verificationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            EmailVerificationCreationResult result = emailVerificationService.createCode(user);

            assertThat(result).isNotNull();
            assertThat(result.rawCode()).isEqualTo(RAW_CODE);
            assertThat(result.verification()).isNotNull();

            then(userRepository).should().save(user);
            then(verificationRepository).should().markAllCodesUsedForUser(user.getId());
            then(verificationRepository).should().save(any(EmailVerification.class));
        }

        @Test
        @DisplayName("deve invalidar códigos anteriores antes de criar novo")
        void createCode_existingCodes_invalidatesOldCodesFirst() {
            User user = mock(User.class);
            given(user.getPublicId()).willReturn(UUID.randomUUID());
            given(user.getId()).willReturn(1L);
            given(user.getLastVerificationEmailSentAt()).willReturn(null);
            given(user.getVerificationEmailRequestCount()).willReturn(0);
            given(rateLimiterService.tryConsume(any())).willReturn(probeConsumed());
            given(generateCode.generate()).willReturn(RAW_CODE);
            given(passwordEncoder.encode(any())).willReturn("encoded");
            given(verificationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            emailVerificationService.createCode(user);

            then(verificationRepository).should().markAllCodesUsedForUser(user.getId());
        }

        @Test
        @DisplayName("deve resetar contagem quando última solicitação foi há mais de 1 hora")
        void createCode_lastSentMoreThanOneHourAgo_resetsRequestCount() {
            User user = mock(User.class);
            given(user.getPublicId()).willReturn(UUID.randomUUID());
            given(user.getId()).willReturn(1L);
            given(user.getLastVerificationEmailSentAt())
                    .willReturn(Instant.now().minus(2, ChronoUnit.HOURS));
            given(user.getVerificationEmailRequestCount()).willReturn(3);
            given(rateLimiterService.tryConsume(any())).willReturn(probeConsumed());
            given(generateCode.generate()).willReturn(RAW_CODE);
            given(passwordEncoder.encode(any())).willReturn("encoded");
            given(verificationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            emailVerificationService.createCode(user);

            then(user).should().resetEmailVerificationRequests();
        }

        @Test
        @DisplayName("deve lançar RateLimitExceededException quando cooldown entre envios está ativo")
        void createCode_withinCooldownWindow_throwsRateLimitExceededException() {
            User user = mock(User.class);
            given(user.getPublicId()).willReturn(UUID.randomUUID());
            given(user.getLastVerificationEmailSentAt())
                    .willReturn(Instant.now().minus(1, ChronoUnit.MINUTES));
            given(user.getVerificationEmailRequestCount()).willReturn(1);

            assertThatExceptionOfType(RateLimitExceededException.class)
                    .isThrownBy(() -> emailVerificationService.createCode(user));

            then(rateLimiterService).should(never()).tryConsume(any());
            then(verificationRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve lançar RateLimitExceededException quando bucket do rate limiter está esgotado")
        void createCode_bucketExhausted_throwsRateLimitExceededException() {
            User user = mock(User.class);
            given(user.getPublicId()).willReturn(UUID.randomUUID());
            given(user.getLastVerificationEmailSentAt()).willReturn(null);
            given(user.getVerificationEmailRequestCount()).willReturn(0);
            given(rateLimiterService.tryConsume(any())).willReturn(probeExhausted());

            assertThatExceptionOfType(RateLimitExceededException.class)
                    .isThrownBy(() -> emailVerificationService.createCode(user));

            then(verificationRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve permitir novo envio quando cooldown já expirou")
        void createCode_cooldownExpired_createsCodeSuccessfully() {
            User user = mock(User.class);
            given(user.getPublicId()).willReturn(UUID.randomUUID());
            given(user.getId()).willReturn(1L);
            // requestCount=1 → delay=240s; last sent há 300s → cooldown expirou
            given(user.getLastVerificationEmailSentAt())
                    .willReturn(Instant.now().minus(300, ChronoUnit.SECONDS));
            given(user.getVerificationEmailRequestCount()).willReturn(1);
            given(rateLimiterService.tryConsume(any())).willReturn(probeConsumed());
            given(generateCode.generate()).willReturn(RAW_CODE);
            given(passwordEncoder.encode(any())).willReturn("encoded");
            given(verificationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            EmailVerificationCreationResult result = emailVerificationService.createCode(user);

            assertThat(result.rawCode()).isEqualTo(RAW_CODE);
        }
    }

    // --- VALIDATE CODE -------------------------------------------------------------

    @Nested
    @DisplayName("validateCode()")
    class ValidateCode {

        @Test
        @DisplayName("deve validar código com sucesso e marcar email como verificado")
        void validateCode_validCode_marksEmailAsVerified() {
            User user = mock(User.class);
            given(user.getPublicId()).willReturn(UUID.randomUUID());
            given(user.isEmailVerified()).willReturn(false);

            EmailVerification verification = mock(EmailVerification.class);
            given(verification.getVerificationCode()).willReturn("encoded-code");
            given(verification.getExpiresAt()).willReturn(Instant.now().plus(10, ChronoUnit.MINUTES));
            given(verification.getUser()).willReturn(user);

            given(verificationRepository.findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(1L))
                    .willReturn(Optional.of(verification));
            given(passwordEncoder.matches(RAW_CODE, "encoded-code")).willReturn(true);

            emailVerificationService.validateCode(1L, RAW_CODE);

            then(verification).should().markEmailAsUsed();
            then(user).should().markEmailAsVerified();
            then(user).should().resetEmailVerificationRequests();
            then(userRepository).should().save(user);
            then(verificationRepository).should().save(verification);
        }

        @Test
        @DisplayName("deve lançar InvalidCodeException quando não existe código ativo")
        void validateCode_noActiveCode_throwsInvalidCodeException() {
            given(verificationRepository.findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(1L))
                    .willReturn(Optional.empty());

            assertThatExceptionOfType(InvalidCodeException.class)
                    .isThrownBy(() -> emailVerificationService.validateCode(1L, RAW_CODE));

            then(passwordEncoder).should(never()).matches(any(), any());
        }

        @Test
        @DisplayName("deve lançar InvalidCodeException quando código não corresponde ao hash")
        void validateCode_codeMismatch_throwsInvalidCodeException() {
            EmailVerification verification = mock(EmailVerification.class);
            given(verification.getVerificationCode()).willReturn("encoded-code");

            given(verificationRepository.findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(1L))
                    .willReturn(Optional.of(verification));
            given(passwordEncoder.matches("wrong-code", "encoded-code")).willReturn(false);

            assertThatExceptionOfType(InvalidCodeException.class)
                    .isThrownBy(() -> emailVerificationService.validateCode(1L, "wrong-code"));

            then(verification).should(never()).markEmailAsUsed();
        }

        @Test
        @DisplayName("deve lançar InvalidCodeException quando código está expirado")
        void validateCode_expiredCode_throwsInvalidCodeException() {
            User user = mock(User.class);
            given(user.getPublicId()).willReturn(UUID.randomUUID());

            EmailVerification verification = mock(EmailVerification.class);
            given(verification.getVerificationCode()).willReturn("encoded-code");
            given(verification.getExpiresAt())
                    .willReturn(Instant.now().minus(1, ChronoUnit.MINUTES));

            given(verification.getUser()).willReturn(user);

            given(verificationRepository.findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(1L))
                    .willReturn(Optional.of(verification));

            given(passwordEncoder.matches(RAW_CODE, "encoded-code"))
                    .willReturn(true);

            assertThatExceptionOfType(InvalidCodeException.class)
                    .isThrownBy(() -> emailVerificationService.validateCode(1L, RAW_CODE));

            then(verification).should(never()).markEmailAsUsed();
        }

        @Test
        @DisplayName("deve lançar EmailAlreadyVerifiedException quando email já estava verificado")
        void validateCode_emailAlreadyVerified_throwsEmailAlreadyVerifiedException() {
            User user = mock(User.class);
            given(user.isEmailVerified()).willReturn(true);

            EmailVerification verification = mock(EmailVerification.class);
            given(verification.getVerificationCode()).willReturn("encoded-code");
            given(verification.getExpiresAt()).willReturn(Instant.now().plus(10, ChronoUnit.MINUTES));
            given(verification.getUser()).willReturn(user);

            given(verificationRepository.findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(1L))
                    .willReturn(Optional.of(verification));
            given(passwordEncoder.matches(RAW_CODE, "encoded-code")).willReturn(true);

            assertThatExceptionOfType(EmailAlreadyVerifiedException.class)
                    .isThrownBy(() -> emailVerificationService.validateCode(1L, RAW_CODE));

            then(user).should(never()).markEmailAsVerified();
        }
    }

    // --- CLEAN EXPIRED CODES -------------------------------------------------------

    @Nested
    @DisplayName("cleanExpiresCodes()")
    class CleanExpiresCodes {

        @Test
        @DisplayName("deve delegar a limpeza ao repositório com o instante atual")
        void cleanExpiresCodes_always_callsRepositoryWithCurrentInstant() {
            emailVerificationService.cleanExpiresCodes();

            then(verificationRepository).should().deleteExpiresCode(any(Instant.class));
        }
    }
}