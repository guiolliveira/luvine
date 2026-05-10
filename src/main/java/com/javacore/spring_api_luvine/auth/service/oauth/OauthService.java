package com.javacore.spring_api_luvine.auth.service.oauth;

import com.javacore.spring_api_luvine.auth.domain.exception.ProviderConflictException;
import com.javacore.spring_api_luvine.auth.dto.LoginResponse;
import com.javacore.spring_api_luvine.auth.service.TokenService;
import com.javacore.spring_api_luvine.shared.util.EmailMask;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.entity.UserProvider;
import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import com.javacore.spring_api_luvine.user.domain.valueObject.Name;
import com.javacore.spring_api_luvine.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OauthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    public LoginResponse loginWithGoogle(String email, String name, String deviceInfo, String ipAddress) {
        String maskedEmail = EmailMask.mask(email);
        log.info("event=oauth_login_attempt provider=GOOGLE email={} ip={}", maskedEmail, ipAddress);

        Email normalizedEmail = new Email(email);

        User user = userRepository.findByEmail(normalizedEmail.value())
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

                    Name normalizedFullName = new Name(name);

                    String[] parts = normalizedFullName.value().split(" ");
                    String firstName = parts[0];
                    String lastName = parts.length > 1 ? parts[parts.length - 1] : "";

                    User newUser = User.create(
                            normalizedEmail.value(),
                            firstName,
                            lastName,
                            "",
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