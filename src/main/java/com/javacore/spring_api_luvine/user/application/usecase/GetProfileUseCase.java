package com.javacore.spring_api_luvine.user.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.user.application.dto.CurrentUser;
import com.javacore.spring_api_luvine.user.application.dto.ProfileResponse;
import com.javacore.spring_api_luvine.user.application.mapper.UserMapper;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.exception.UserSessionInvalidException;
import com.javacore.spring_api_luvine.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class GetProfileUseCase {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public ProfileResponse execute(CurrentUser currentUser) {
        log.info("event=find_profile_attempt publicId={}", currentUser.publicId());

        User user = userRepository.findByPublicId(currentUser.publicId())
                .orElseThrow(() -> {
                    log.warn("event=user_not_found publicId={}", currentUser.publicId());
                    return new UserSessionInvalidException();
                });

        log.info("event=find_profile_success publicId={}", user.getPublicId());
        return userMapper.toProfileResponse(user);
    }
}