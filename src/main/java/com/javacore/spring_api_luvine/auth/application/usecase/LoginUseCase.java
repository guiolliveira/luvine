package com.javacore.spring_api_luvine.auth.application.usecase;

import com.javacore.spring_api_luvine.auth.application.dto.LoginRequest;
import com.javacore.spring_api_luvine.auth.application.dto.LoginResponse;
import com.javacore.spring_api_luvine.auth.application.service.TokenService;
import com.javacore.spring_api_luvine.auth.domain.exception.EmailNotVerifiedException;
import com.javacore.spring_api_luvine.auth.domain.exception.InvalidCredentialsException;
import com.javacore.spring_api_luvine.auth.domain.exception.ProviderConflictException;
import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.common.util.EmailMask;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.entity.UserProvider;
import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import com.javacore.spring_api_luvine.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class LoginUseCase {

    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public LoginResponse execute(LoginRequest request, String deviceInfo, String ipAddress) {
        String maskedEmail = EmailMask.mask(request.email());
        log.info("event=login_attempt email={} ip={}", maskedEmail, ipAddress);

        Email email = new Email(request.email());

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    email.value(),
                    request.password()
            ));
        } catch (AuthenticationException ex) {
            log.warn("event=login_failed reason=invalid_credentials email={} ip={}", maskedEmail, ipAddress);
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (user.getUserProvider() != UserProvider.LOCAL) {
            log.warn("event=login_rejected reason=provider_conflict publicId={} provider={}",
                    user.getPublicId(), user.getUserProvider());
            throw new ProviderConflictException();
        }

        if (!user.isEmailVerified()) {
            log.warn("event=login_rejected reason=email_not_verified publicId={} email={}",
                    user.getPublicId(), maskedEmail);
            throw new EmailNotVerifiedException();
        }

        String refreshToken = tokenService.generateRefreshToken(user, deviceInfo, ipAddress);
        String accessToken = tokenService.generateAccessToken(user);

        log.info("event=login_success publicId={} email={} ip={}", user.getPublicId(), maskedEmail, ipAddress);
        return new LoginResponse(accessToken, refreshToken);
    }
}