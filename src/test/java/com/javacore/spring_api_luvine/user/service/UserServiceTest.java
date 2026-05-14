package com.javacore.spring_api_luvine.user.service;

import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.entity.UserProvider;
import com.javacore.spring_api_luvine.user.domain.entity.UserRole;
import com.javacore.spring_api_luvine.user.domain.exception.*;
import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import com.javacore.spring_api_luvine.user.domain.valueObject.Name;
import com.javacore.spring_api_luvine.user.dto.CurrentUser;
import com.javacore.spring_api_luvine.user.dto.ProfileResponse;
import com.javacore.spring_api_luvine.user.dto.UpdateProfileRequest;
import com.javacore.spring_api_luvine.user.dto.UpdateRoleRequest;
import com.javacore.spring_api_luvine.user.mapper.UserMapper;
import com.javacore.spring_api_luvine.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@DisplayName("UserService")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID USER_PUBLIC_ID   = UUID.randomUUID();
    private static final UUID TARGET_PUBLIC_ID = UUID.randomUUID();

    private CurrentUser currentUser() {
        return new CurrentUser(USER_PUBLIC_ID);
    }

    private User buildUser() {
        return User.create(
                new Email("user@example.com"),
                new Name("João"),
                new Name("Silva"),
                "hashed-password",
                UserProvider.LOCAL
        );
    }

    private User buildUserWithRole(UserRole role) {
        User user = buildUser();
        user.changeRole(role);
        return user;
    }

    // --- FIND PROFILE ------------------------------------------------------------------

    @Nested
    @DisplayName("findProfile()")
    class FindProfile {

        @Test
        @DisplayName("deve retornar ProfileResponse quando usuário encontrado")
        void findProfile_existingUser_returnsProfileResponse() {
            User user = buildUser();
            ProfileResponse expectedResponse = mock(ProfileResponse.class);

            given(userRepository.findByPublicId(USER_PUBLIC_ID)).willReturn(Optional.of(user));
            given(userMapper.toProfileResponse(user)).willReturn(expectedResponse);

            ProfileResponse response = userService.findProfile(currentUser());

            assertThat(response).isEqualTo(expectedResponse);
        }

        @Test
        @DisplayName("deve lançar UserSessionInvalidException quando usuário não encontrado")
        void findProfile_userNotFound_throwsUserSessionInvalidException() {
            given(userRepository.findByPublicId(USER_PUBLIC_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(UserSessionInvalidException.class)
                    .isThrownBy(() -> userService.findProfile(currentUser()));

            then(userMapper).should(never()).toProfileResponse(any());
        }

        @Test
        @DisplayName("deve mapear o usuário exatamente uma vez")
        void findProfile_existingUser_mapsUserExactlyOnce() {
            User user = buildUser();
            given(userRepository.findByPublicId(USER_PUBLIC_ID)).willReturn(Optional.of(user));
            given(userMapper.toProfileResponse(user)).willReturn(mock(ProfileResponse.class));

            userService.findProfile(currentUser());

            then(userMapper).should(times(1)).toProfileResponse(user);
        }
    }

    // --- UPDATE PROFILE ------------------------------------------------------------------

    @Nested
    @DisplayName("updateProfile()")
    class UpdateProfile {

        @Test
        @DisplayName("deve atualizar firstName com sucesso quando valor é diferente")
        void updateProfile_newFirstName_updatesSuccessfully() {
            User user = buildUser();
            UpdateProfileRequest request = new UpdateProfileRequest("NovoNome", null);

            given(userRepository.findByPublicId(USER_PUBLIC_ID)).willReturn(Optional.of(user));

            assertThatNoException().isThrownBy(() -> userService.updateProfile(currentUser(), request));

            assertThat(user.getFirstName()).isEqualTo(new Name("NovoNome"));
        }

        @Test
        @DisplayName("deve atualizar lastName com sucesso quando valor é diferente")
        void updateProfile_newLastName_updatesSuccessfully() {
            User user = buildUser();
            UpdateProfileRequest request = new UpdateProfileRequest(null, "NovoSobrenome");

            given(userRepository.findByPublicId(USER_PUBLIC_ID)).willReturn(Optional.of(user));

            assertThatNoException().isThrownBy(() -> userService.updateProfile(currentUser(), request));

            assertThat(user.getLastName()).isEqualTo(new Name("NovoSobrenome"));
        }

        @Test
        @DisplayName("deve atualizar firstName e lastName simultaneamente")
        void updateProfile_bothFields_updatesBoth() {
            User user = buildUser();
            UpdateProfileRequest request = new UpdateProfileRequest("NovoNome", "NovoSobrenome");

            given(userRepository.findByPublicId(USER_PUBLIC_ID)).willReturn(Optional.of(user));

            userService.updateProfile(currentUser(), request);

            assertThat(user.getFirstName()).isEqualTo(new Name("NovoNome"));
            assertThat(user.getLastName()).isEqualTo(new Name("NovoSobrenome"));
        }

        @Test
        @DisplayName("deve lançar UnchangedValueException quando firstName não muda")
        void updateProfile_sameFirstName_throwsUnchangedValueException() {
            User user = buildUser();
            UpdateProfileRequest request = new UpdateProfileRequest("João", null);

            given(userRepository.findByPublicId(USER_PUBLIC_ID)).willReturn(Optional.of(user));

            assertThatExceptionOfType(UnchangedValueException.class)
                    .isThrownBy(() -> userService.updateProfile(currentUser(), request));
        }

        @Test
        @DisplayName("deve lançar UnchangedValueException quando lastName não muda")
        void updateProfile_sameLastName_throwsUnchangedValueException() {
            User user = buildUser();
            UpdateProfileRequest request = new UpdateProfileRequest(null, "Silva");

            given(userRepository.findByPublicId(USER_PUBLIC_ID)).willReturn(Optional.of(user));

            assertThatExceptionOfType(UnchangedValueException.class)
                    .isThrownBy(() -> userService.updateProfile(currentUser(), request));
        }

        @Test
        @DisplayName("não deve alterar nenhum campo quando firstName e lastName são nulos")
        void updateProfile_bothNull_doesNothing() {
            User user = buildUser();
            UpdateProfileRequest request = new UpdateProfileRequest(null, null);

            given(userRepository.findByPublicId(USER_PUBLIC_ID)).willReturn(Optional.of(user));

            assertThatNoException().isThrownBy(() -> userService.updateProfile(currentUser(), request));

            assertThat(user.getFirstName()).isEqualTo(new Name("João"));
            assertThat(user.getLastName()).isEqualTo(new Name("Silva"));
        }

        @Test
        @DisplayName("deve lançar UserSessionInvalidException quando usuário não encontrado")
        void updateProfile_userNotFound_throwsUserSessionInvalidException() {
            given(userRepository.findByPublicId(USER_PUBLIC_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(UserSessionInvalidException.class)
                    .isThrownBy(() -> userService.updateProfile(currentUser(),
                            new UpdateProfileRequest("NovoNome", null)));
        }
    }

    // --- UPDATE ROLE ------------------------------------------------------------------

    @Nested
    @DisplayName("updateRole()")
    class UpdateRole {

        @Test
        @DisplayName("deve lançar SelfPromotionNotAllowedException quando actor tenta alterar o próprio role")
        void updateRole_selfPromotion_throwsSelfPromotionNotAllowedException() {
            UpdateRoleRequest request = new UpdateRoleRequest(UserRole.ADMIN);

            assertThatExceptionOfType(SelfPromotionNotAllowedException.class)
                    .isThrownBy(() -> userService.updateRole(currentUser(), USER_PUBLIC_ID, request));

            then(userRepository).should(never()).findByPublicId(any());
        }

        @Test
        @DisplayName("deve lançar UserSessionInvalidException quando actor não encontrado")
        void updateRole_actorNotFound_throwsUserSessionInvalidException() {
            UpdateRoleRequest request = new UpdateRoleRequest(UserRole.ADMIN);

            given(userRepository.findByPublicId(USER_PUBLIC_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(UserSessionInvalidException.class)
                    .isThrownBy(() -> userService.updateRole(currentUser(), TARGET_PUBLIC_ID, request));
        }

        @Test
        @DisplayName("deve lançar UserSessionInvalidException quando target não encontrado")
        void updateRole_targetNotFound_throwsUserSessionInvalidException() {
            User actor = buildUserWithRole(UserRole.ADMIN);
            UpdateRoleRequest request = new UpdateRoleRequest(UserRole.CUSTOMER);

            given(userRepository.findByPublicId(USER_PUBLIC_ID)).willReturn(Optional.of(actor));
            given(userRepository.findByPublicId(TARGET_PUBLIC_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(UserSessionInvalidException.class)
                    .isThrownBy(() -> userService.updateRole(currentUser(), TARGET_PUBLIC_ID, request));
        }

        @Test
        @DisplayName("deve lançar InsufficientPromotionsException quando actor não tem autoridade suficiente")
        void updateRole_insufficientAuthority_throwsInsufficientPromotionsException() {
            User actor = buildUserWithRole(UserRole.CUSTOMER);
            User target = buildUserWithRole(UserRole.ADMIN);
            UpdateRoleRequest request = new UpdateRoleRequest(UserRole.CUSTOMER);

            given(userRepository.findByPublicId(USER_PUBLIC_ID)).willReturn(Optional.of(actor));
            given(userRepository.findByPublicId(TARGET_PUBLIC_ID)).willReturn(Optional.of(target));

            assertThatExceptionOfType(InsufficientPromotionsException.class)
                    .isThrownBy(() -> userService.updateRole(currentUser(), TARGET_PUBLIC_ID, request));
        }

        @Test
        @DisplayName("deve lançar RoleAlreadyAssignedException quando target já possui o role solicitado")
        void updateRole_roleAlreadyAssigned_throwsRoleAlreadyAssignedException() {
            User actor = buildUserWithRole(UserRole.ADMIN);
            User target = buildUserWithRole(UserRole.CUSTOMER);
            UpdateRoleRequest request = new UpdateRoleRequest(UserRole.CUSTOMER);

            given(userRepository.findByPublicId(USER_PUBLIC_ID)).willReturn(Optional.of(actor));
            given(userRepository.findByPublicId(TARGET_PUBLIC_ID)).willReturn(Optional.of(target));

            assertThatExceptionOfType(RoleAlreadyAssignedException.class)
                    .isThrownBy(() -> userService.updateRole(currentUser(), TARGET_PUBLIC_ID, request));
        }

        @Test
        @DisplayName("deve alterar o role do target com sucesso")
        void updateRole_validRequest_changesTargetRole() {
            User actor = buildUserWithRole(UserRole.ADMIN);
            User target = buildUserWithRole(UserRole.CUSTOMER);
            UpdateRoleRequest request = new UpdateRoleRequest(UserRole.ADMIN);

            given(userRepository.findByPublicId(USER_PUBLIC_ID)).willReturn(Optional.of(actor));
            given(userRepository.findByPublicId(TARGET_PUBLIC_ID)).willReturn(Optional.of(target));

            assertThatNoException()
                    .isThrownBy(() -> userService.updateRole(currentUser(), TARGET_PUBLIC_ID, request));

            assertThat(target.getUserRole()).isEqualTo(UserRole.ADMIN);
        }

        @Test
        @DisplayName("não deve alterar o role do actor ao promover o target")
        void updateRole_validRequest_doesNotChangeActorRole() {
            User actor = buildUserWithRole(UserRole.ADMIN);
            User target = buildUserWithRole(UserRole.CUSTOMER);
            UpdateRoleRequest request = new UpdateRoleRequest(UserRole.ADMIN);

            given(userRepository.findByPublicId(USER_PUBLIC_ID)).willReturn(Optional.of(actor));
            given(userRepository.findByPublicId(TARGET_PUBLIC_ID)).willReturn(Optional.of(target));

            userService.updateRole(currentUser(), TARGET_PUBLIC_ID, request);

            assertThat(actor.getUserRole()).isEqualTo(UserRole.ADMIN);
        }
    }
}