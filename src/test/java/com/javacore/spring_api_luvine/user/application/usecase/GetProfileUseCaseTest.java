package com.javacore.spring_api_luvine.user.application.usecase;

import com.javacore.spring_api_luvine.user.application.dto.CurrentUser;
import com.javacore.spring_api_luvine.user.application.dto.ProfileResponse;
import com.javacore.spring_api_luvine.user.application.mapper.UserMapper;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.entity.UserRole;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.BDDMockito.*;

@DisplayName("GetProfileUseCase")
@ExtendWith(MockitoExtension.class)
class GetProfileUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private GetProfileUseCase getProfileUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID PUBLIC_ID = UUID.randomUUID();

    private CurrentUser currentUser() {
        return new CurrentUser(PUBLIC_ID);
    }

    private ProfileResponse buildProfileResponse() {
        return new ProfileResponse(PUBLIC_ID, "user@example.com", "João", "Silva", UserRole.CUSTOMER, "JS");
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve retornar ProfileResponse quando usuário encontrado")
        void execute_userFound_returnsProfileResponse() {
            User user = mock(User.class);
            ProfileResponse expected = buildProfileResponse();

            given(userRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(user));
            given(userMapper.toProfileResponse(user)).willReturn(expected);

            ProfileResponse result = getProfileUseCase.execute(currentUser());

            assertThat(result).isNotNull().isEqualTo(expected);
        }

        @Test
        @DisplayName("deve lançar UserSessionInvalidException quando usuário não encontrado")
        void execute_userNotFound_throwsUserSessionInvalidException() {
            given(userRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(UserSessionInvalidException.class)
                    .isThrownBy(() -> getProfileUseCase.execute(currentUser()));

            then(userMapper).should(never()).toProfileResponse(any());
        }

        @Test
        @DisplayName("deve mapear o usuário encontrado usando o userMapper")
        void execute_userFound_mapsUserWithMapper() {
            User user = mock(User.class);

            given(userRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(user));
            given(userMapper.toProfileResponse(user)).willReturn(buildProfileResponse());

            getProfileUseCase.execute(currentUser());

            then(userMapper).should(times(1)).toProfileResponse(user);
        }

        @Test
        @DisplayName("deve buscar usuário usando o publicId correto da sessão")
        void execute_called_queriesRepositoryWithCorrectPublicId() {
            User user = mock(User.class);

            given(userRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(user));
            given(userMapper.toProfileResponse(user)).willReturn(buildProfileResponse());

            getProfileUseCase.execute(currentUser());

            then(userRepository).should().findByPublicId(PUBLIC_ID);
        }
    }
}