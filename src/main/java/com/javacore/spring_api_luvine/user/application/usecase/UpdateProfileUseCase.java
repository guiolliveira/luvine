package com.javacore.spring_api_luvine.user.application.usecase;

import com.javacore.spring_api_luvine.common.config.UseCase;
import com.javacore.spring_api_luvine.user.application.dto.CurrentUser;
import com.javacore.spring_api_luvine.user.application.dto.UpdateProfileRequest;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.exception.NoProfileChangesProvidedException;
import com.javacore.spring_api_luvine.user.domain.exception.UserSessionInvalidException;
import com.javacore.spring_api_luvine.user.domain.valueObject.PersonName;
import com.javacore.spring_api_luvine.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class UpdateProfileUseCase {

    private final UserRepository userRepository;

    @Transactional
    public void execute(CurrentUser currentUser, UpdateProfileRequest request) {
        log.info("event=update_profile_attempt publicId={}", currentUser.publicId());

        if (request.newFirstName() == null && request.newLastName() == null) {
            throw new NoProfileChangesProvidedException();
        }

        User user = userRepository.findByPublicId(currentUser.publicId())
                .orElseThrow(() -> {
                    log.warn("event=user_not_found publicId={}", currentUser.publicId());
                    return new UserSessionInvalidException();
                });

        if (request.newFirstName() != null) {
            user.changeFirstName(new PersonName(request.newFirstName()));
            log.info("event=update_profile_first_name_changed publicId={}", user.getPublicId());
        }

        if (request.newLastName() != null) {
            user.changeLastName(new PersonName(request.newLastName()));
            log.info("event=update_profile_last_name_changed publicId={}", user.getPublicId());
        }

        log.info("event=update_profile_success publicId={}", user.getPublicId());
    }
}