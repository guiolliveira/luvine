package com.javacore.spring_api_luvine.user.application.usecase;

import com.javacore.spring_api_luvine.user.application.dto.CurrentUser;
import com.javacore.spring_api_luvine.user.application.dto.UpdateRoleRequest;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.entity.UserRole;
import com.javacore.spring_api_luvine.user.domain.exception.InsufficientPromotionsException;
import com.javacore.spring_api_luvine.user.domain.exception.RoleAlreadyAssignedException;
import com.javacore.spring_api_luvine.user.domain.exception.SelfPromotionNotAllowedException;
import com.javacore.spring_api_luvine.user.domain.exception.UserSessionInvalidException;
import com.javacore.spring_api_luvine.user.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@DisplayName("UpdateUserRoleUseCase")
@ExtendWith(MockitoExtension.class)
class UpdateUserRoleUseCaseTest {

    @Mock private UserRepository userRepository;

    @InjectMocks
    private UpdateUserRoleUseCase updateUserRoleUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID ACTOR_PUBLIC_ID = UUID.randomUUID();
    private static final UUID TARGET_PUBLIC_ID = UUID.randomUUID();

    private CurrentUser currentUser() {
        return new CurrentUser(ACTOR_PUBLIC_ID);
    }

    // mockUserWithRole: para testes onde o fluxo não chega no log de sucesso
    private User mockUserWithRole(UserRole role) {
        User user = mock(User.class);
        given(user.getUserRole()).willReturn(role);
        return user;
    }

    // mockUserWithRoleAndId: para testes onde o fluxo chega no log de sucesso (usa getPublicId)
    private User mockUserWithRoleAndId(UserRole role) {
        User user = mock(User.class);
        given(user.getUserRole()).willReturn(role);
        given(user.getPublicId()).willReturn(UUID.randomUUID());
        return user;
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve lançar SelfPromotionNotAllowedException quando actor tenta promover a si mesmo")
        void execute_selfPromotion_throwsSelfPromotionNotAllowedException() {
            assertThatExceptionOfType(SelfPromotionNotAllowedException.class)
                    .isThrownBy(() -> updateUserRoleUseCase.execute(
                            currentUser(), ACTOR_PUBLIC_ID, new UpdateRoleRequest(UserRole.ADMIN)));

            then(userRepository).should(never()).findByPublicId(any());
        }

        @Test
        @DisplayName("deve lançar UserSessionInvalidException quando actor não encontrado")
        void execute_actorNotFound_throwsUserSessionInvalidException() {
            given(userRepository.findByPublicId(ACTOR_PUBLIC_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(UserSessionInvalidException.class)
                    .isThrownBy(() -> updateUserRoleUseCase.execute(
                            currentUser(), TARGET_PUBLIC_ID, new UpdateRoleRequest(UserRole.ADMIN)));
        }

        @Test
        @DisplayName("deve lançar UserSessionInvalidException quando target não encontrado")
        void execute_targetNotFound_throwsUserSessionInvalidException() {
            given(userRepository.findByPublicId(ACTOR_PUBLIC_ID)).willReturn(Optional.of(mock(User.class)));
            given(userRepository.findByPublicId(TARGET_PUBLIC_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(UserSessionInvalidException.class)
                    .isThrownBy(() -> updateUserRoleUseCase.execute(
                            currentUser(), TARGET_PUBLIC_ID, new UpdateRoleRequest(UserRole.ADMIN)));
        }

        @Test
        @DisplayName("deve lançar InsufficientPromotionsException quando actor não tem autoridade superior ao target")
        void execute_actorWithoutHigherAuthority_throwsInsufficientPromotionsException() {
            User actor = mockUserWithRoleAndId(UserRole.CUSTOMER);
            User target = mockUserWithRoleAndId(UserRole.ADMIN);

            given(userRepository.findByPublicId(ACTOR_PUBLIC_ID)).willReturn(Optional.of(actor));
            given(userRepository.findByPublicId(TARGET_PUBLIC_ID)).willReturn(Optional.of(target));

            assertThatExceptionOfType(InsufficientPromotionsException.class)
                    .isThrownBy(() -> updateUserRoleUseCase.execute(
                            currentUser(), TARGET_PUBLIC_ID, new UpdateRoleRequest(UserRole.CUSTOMER)));

            then(target).should(never()).changeRole(any());
        }

        @Test
        @DisplayName("deve lançar RoleAlreadyAssignedException quando role já está atribuída ao target")
        void execute_roleAlreadyAssigned_throwsRoleAlreadyAssignedException() {
            User actor = mockUserWithRole(UserRole.SUPER_ADMIN);
            User target = mockUserWithRole(UserRole.ADMIN);

            given(userRepository.findByPublicId(ACTOR_PUBLIC_ID)).willReturn(Optional.of(actor));
            given(userRepository.findByPublicId(TARGET_PUBLIC_ID)).willReturn(Optional.of(target));

            assertThatExceptionOfType(RoleAlreadyAssignedException.class)
                    .isThrownBy(() -> updateUserRoleUseCase.execute(
                            currentUser(), TARGET_PUBLIC_ID, new UpdateRoleRequest(UserRole.ADMIN)));

            then(target).should(never()).changeRole(any());
        }

        @Test
        @DisplayName("deve atualizar o role do target quando todas as validações passam")
        void execute_validRequest_changesTargetRole() {
            User actor = mockUserWithRoleAndId(UserRole.SUPER_ADMIN);
            User target = mockUserWithRoleAndId(UserRole.CUSTOMER);

            given(userRepository.findByPublicId(ACTOR_PUBLIC_ID)).willReturn(Optional.of(actor));
            given(userRepository.findByPublicId(TARGET_PUBLIC_ID)).willReturn(Optional.of(target));

            updateUserRoleUseCase.execute(currentUser(), TARGET_PUBLIC_ID, new UpdateRoleRequest(UserRole.ADMIN));

            then(target).should().changeRole(UserRole.ADMIN);
        }

        @Test
        @DisplayName("não deve alterar o role do actor durante a operação")
        void execute_validRequest_doesNotChangeActorRole() {
            User actor = mockUserWithRoleAndId(UserRole.SUPER_ADMIN);
            User target = mockUserWithRoleAndId(UserRole.CUSTOMER);

            given(userRepository.findByPublicId(ACTOR_PUBLIC_ID)).willReturn(Optional.of(actor));
            given(userRepository.findByPublicId(TARGET_PUBLIC_ID)).willReturn(Optional.of(target));

            updateUserRoleUseCase.execute(currentUser(), TARGET_PUBLIC_ID, new UpdateRoleRequest(UserRole.ADMIN));

            then(actor).should(never()).changeRole(any());
        }
    }
}