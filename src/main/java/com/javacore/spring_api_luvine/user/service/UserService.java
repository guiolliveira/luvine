package com.javacore.spring_api_luvine.user.service;

import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.exception.*;
import com.javacore.spring_api_luvine.user.domain.valueObject.Name;
import com.javacore.spring_api_luvine.user.dto.CurrentUser;
import com.javacore.spring_api_luvine.user.dto.ProfileResponse;
import com.javacore.spring_api_luvine.user.dto.UpdateProfileRequest;
import com.javacore.spring_api_luvine.user.dto.UpdateRoleRequest;
import com.javacore.spring_api_luvine.user.mapper.UserMapper;
import com.javacore.spring_api_luvine.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public ProfileResponse findProfile(CurrentUser currentUser) {
        User user = findByPublicIdOrThrow(currentUser.publicId());

        return userMapper.toProfileResponse(user);
    }

    @Transactional
    public void updateProfile(CurrentUser currentUser, UpdateProfileRequest request) {
        User user = findByPublicIdOrThrow(currentUser.publicId());

        if (request.newFirstName() != null) {
            if (user.getFirstName().value().equals(request.newFirstName())) {
                throw new UnchangedValueException();
            }

            user.changeFirstName(new Name(request.newFirstName()));
        }

        if (request.newLastName() != null) {
            if (user.getLastName().value().equals(request.newLastName())) {
                throw new UnchangedValueException();
            }

            user.changeLastName(new Name(request.newLastName()));
        }
    }

    @Transactional
    public void updateRole(CurrentUser currentUser, UUID targetPublicId, UpdateRoleRequest request) {
        if (currentUser.publicId().equals(targetPublicId)) {
            throw new SelfPromotionNotAllowedException();
        }

        User actor = findByPublicIdOrThrow(currentUser.publicId());
        User targetUser = findByPublicIdOrThrow(targetPublicId);

        if (!actor.getUserRole().hasHigherAuthorityTan(targetUser.getUserRole())) {
            throw new InsufficientPromotionsException();
        }

        if (targetUser.getUserRole() == request.role()) {
            throw new RoleAlreadyAssignedException();
        }

        targetUser.changeRole(request.role());
    }

    private User findByPublicIdOrThrow(UUID publicId) {
        return userRepository.findByPublicId(publicId)
                .orElseThrow(UserSessionInvalidException::new);
    }
}