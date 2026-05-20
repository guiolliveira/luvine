package com.javacore.spring_api_luvine.user.application.service;

import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.exception.*;
import com.javacore.spring_api_luvine.user.domain.valueObject.PersonName;
import com.javacore.spring_api_luvine.user.application.dto.CurrentUser;
import com.javacore.spring_api_luvine.user.application.dto.ProfileResponse;
import com.javacore.spring_api_luvine.user.application.dto.UpdateProfileRequest;
import com.javacore.spring_api_luvine.user.application.dto.UpdateRoleRequest;
import com.javacore.spring_api_luvine.user.application.mapper.UserMapper;
import com.javacore.spring_api_luvine.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public ProfileResponse findProfile(CurrentUser currentUser) {
        log.info("event=find_profile_attempt publicId={}", currentUser.publicId());

        User user = findByPublicIdOrThrow(currentUser.publicId());

        log.info("event=find_profile_success publicId={}", user.getPublicId());
        return userMapper.toProfileResponse(user);
    }

    @Transactional
    public void updateProfile(CurrentUser currentUser, UpdateProfileRequest request) {
        log.info("event=update_profile_attempt publicId={}", currentUser.publicId());

        if (request.newFirstName() == null && request.newLastName() == null) {
            throw new NoProfileChangesProvidedException();
        }

        User user = findByPublicIdOrThrow(currentUser.publicId());

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

    @Transactional
    public void updateRole(CurrentUser currentUser, UUID targetPublicId, UpdateRoleRequest request) {
        log.info("event=update_role_attempt actorPublicId={} targetPublicId={} role={}",
                currentUser.publicId(), targetPublicId, request.role());

        if (currentUser.publicId().equals(targetPublicId)) {
            log.warn("event=update_role_rejected reason=self_promotion actorPublicId={}", currentUser.publicId());
            throw new SelfPromotionNotAllowedException();
        }

        User actor = findByPublicIdOrThrow(currentUser.publicId());
        User targetUser = findByPublicIdOrThrow(targetPublicId);

        if (!actor.getUserRole().hasHigherAuthorityTan(targetUser.getUserRole())) {
            log.warn("event=update_role_rejected reason=insufficient_authority " +
                            "actorPublicId={} actorRole={} targetPublicId={} targetRole={}",
                    actor.getPublicId(), actor.getUserRole(), targetUser.getPublicId(), targetUser.getUserRole());
            throw new InsufficientPromotionsException();
        }

        if (targetUser.getUserRole() == request.role()) {
            log.warn("event=update_role_rejected reason=role_already_assigned targetPublicId={} role={}",
                    targetUser.getPublicId(), request.role());
            throw new RoleAlreadyAssignedException();
        }

        targetUser.changeRole(request.role());
        log.info("event=update_role_success actorPublicId={} targetPublicId={} newRole={}",
                actor.getPublicId(), targetUser.getPublicId(), request.role());
    }

    private User findByPublicIdOrThrow(UUID publicId) {
        return userRepository.findByPublicId(publicId)
                .orElseThrow(() -> {
                    log.warn("event=user_not_found publicId={}", publicId);
                    return new UserSessionInvalidException();
                });
    }
}