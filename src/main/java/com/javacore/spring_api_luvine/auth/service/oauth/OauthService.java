package com.javacore.spring_api_luvine.auth.service.oauth;

import com.javacore.spring_api_luvine.auth.domain.exception.ProviderConflictException;
import com.javacore.spring_api_luvine.auth.dto.LoginResponse;
import com.javacore.spring_api_luvine.auth.service.TokenService;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.entity.UserProvider;
import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import com.javacore.spring_api_luvine.user.domain.valueObject.Name;
import com.javacore.spring_api_luvine.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OauthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    public LoginResponse loginWithGoogle(String email, String name, String deviceInfo, String ipAddress) {
        Email normalizedEmail = new Email(email);

        User user = userRepository.findByEmail(normalizedEmail.value())
                .map(existingUser -> {
                    if (existingUser.getUserProvider() != UserProvider.GOOGLE) {
                        throw new ProviderConflictException();
                    }
                    return existingUser;
                })
                .orElseGet(() -> {
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

                    return userRepository.save(newUser);
                });

        String refreshToken = tokenService.generateRefreshToken(
                user,
                deviceInfo,
                ipAddress
        );
        String accessToken = tokenService.generateAccessToken(user);

        return new LoginResponse(accessToken, refreshToken);
    }
}