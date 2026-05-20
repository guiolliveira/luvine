package com.javacore.spring_api_luvine.auth.application.UseCase;

import com.javacore.spring_api_luvine.auth.application.dto.LoginResponse;
import com.javacore.spring_api_luvine.auth.application.service.TokenService;
import com.javacore.spring_api_luvine.auth.domain.exception.ProviderConflictException;
import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.common.util.EmailMask;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.entity.UserProvider;
import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import com.javacore.spring_api_luvine.user.domain.valueObject.Password;
import com.javacore.spring_api_luvine.user.domain.valueObject.PersonName;
import com.javacore.spring_api_luvine.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class OauthLoginUseCase {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse execute(String email, String name, String deviceInfo, String ipAddress) {
        String maskedEmail = EmailMask.mask(email);
        log.info("event=oauth_login_attempt provider=GOOGLE email={} ip={}", maskedEmail, ipAddress);

        Email normalizedEmail = new Email(email);

        User user = userRepository.findByEmail(normalizedEmail)
                .map(existingUser -> {
                    if (existingUser.getUserProvider() != UserProvider.GOOGLE) {
                        log.warn("event=oauth_login_rejected reason=provider_conflict publicId={} email={}" +
                                        " expectedProvider=GOOGLE actualProvider={}",
                                existingUser.getPublicId(), maskedEmail, existingUser.getUserProvider());
                        throw new ProviderConflictException();
                    }
                    log.debug("event=oauth_existing_user_found publicId={} email={}",
                            existingUser.getPublicId(), maskedEmail);
                    return existingUser;
                })
                .orElseGet(() -> {
                    log.info("event=oauth_new_user_signup provider=GOOGLE email={}", maskedEmail);

                    String sanitizedName = name
                            .replaceAll("[^\\p{L}\\s]", "")
                            .replaceAll("\\s+", "")
                            .trim();

                    PersonName normalizedFullName = new PersonName(sanitizedName);

                    String[] parts = normalizedFullName.value().split(" ");
                    String firstName = parts[0];
                    String lastName = parts.length > 1 ? parts[parts.length - 1] : firstName;

                    User newUser = User.create(
                            normalizedEmail,
                            new PersonName(firstName),
                            new PersonName(lastName),
                            new Password(passwordEncoder.encode(UUID.randomUUID().toString())),
                            UserProvider.GOOGLE
                    );

                    newUser.markEmailAsVerified();

                    User saved = userRepository.save(newUser);
                    log.info("event=oauth_user_created provider=GOOGLE publicId={} email={}",
                            saved.getPublicId(), maskedEmail);
                    return saved;
                });

        String refreshToken = tokenService.generateRefreshToken(user, deviceInfo, ipAddress);
        String accessToken = tokenService.generateAccessToken(user);

        log.info("event=oauth_login_success provider=GOOGLE publicId={} email={} ip={}",
                user.getPublicId(), maskedEmail, ipAddress);
        return new LoginResponse(accessToken, refreshToken);
    }
}