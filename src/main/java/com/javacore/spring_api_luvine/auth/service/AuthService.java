package com.javacore.spring_api_luvine.auth.service;

import com.javacore.spring_api_luvine.auth.domain.entity.RefreshToken;
import com.javacore.spring_api_luvine.auth.domain.exception.EmailAlreadyExistsException;
import com.javacore.spring_api_luvine.auth.domain.exception.InvalidCredentialsException;
import com.javacore.spring_api_luvine.auth.domain.exception.InvalidRefreshTokenException;
import com.javacore.spring_api_luvine.auth.domain.exception.PasswordMisMatchException;
import com.javacore.spring_api_luvine.auth.dto.LoginRequest;
import com.javacore.spring_api_luvine.auth.dto.LoginResponse;
import com.javacore.spring_api_luvine.auth.dto.RegisterRequest;
import com.javacore.spring_api_luvine.auth.dto.RegisterResponse;
import com.javacore.spring_api_luvine.auth.mapper.AuthMapper;
import com.javacore.spring_api_luvine.auth.repository.RefreshTokenRepository;
import com.javacore.spring_api_luvine.shared.util.TokenHash;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import com.javacore.spring_api_luvine.user.domain.valueObject.Name;
import com.javacore.spring_api_luvine.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        Email email = new Email(request.email());
        Name firstName = new Name(request.firstName());
        Name lastName = new Name(request.lastName());

        if (userRepository.existsByEmail(email.value())) {
            throw new EmailAlreadyExistsException();
        }

        if (!request.password().equals(request.confirmPassword())) {
            throw new PasswordMisMatchException();
        }

        User user = User.create(
                email.value(),
                firstName.value(),
                lastName.value(),
                passwordEncoder.encode(request.password())
        );

        userRepository.save(user);

        return authMapper.toRegisterResponse(user);
    }

    public LoginResponse login(LoginRequest request, String deviceInfo, String ipAddress) {
        Email email = new Email(request.email());

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    email.value(),
                    request.password()
            ));
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByEmail(email.value())
                .orElseThrow(InvalidCredentialsException::new);

        String refreshToken = tokenService.generateRefreshToken(
                user,
                deviceInfo,
                ipAddress
        );
        String accessToken = tokenService.generateAccessToken(user);

        return new LoginResponse(accessToken, refreshToken);
    }

    public LoginResponse refresh(String refreshToken) {
        String tokenHash = TokenHash.hash(refreshToken);

        RefreshToken token = refreshTokenRepository.findByToken(tokenHash)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (token.isRevoked()) {
            throw new InvalidRefreshTokenException();
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException();
        }

        token.revoke();

        String newRefreshToken = tokenService.generateRefreshToken(
                token.getUser(),
                token.getDeviceInfo(),
                token.getIpAddress()
        );

        String newTokenHash = TokenHash.hash(newRefreshToken);
        token.markAsReplacedBy(newTokenHash);

        String newAccessToken = tokenService.generateAccessToken(token.getUser());

        return new LoginResponse(newAccessToken, newRefreshToken);
    }
}