package com.javacore.spring_api_luvine.user.application.usecase;

import com.javacore.spring_api_luvine.user.application.dto.CurrentUser;
import com.javacore.spring_api_luvine.user.application.dto.UpdateProfileRequest;
import com.javacore.spring_api_luvine.user.domain.entity.User;
import com.javacore.spring_api_luvine.user.domain.exception.NoProfileChangesProvidedException;
import com.javacore.spring_api_luvine.user.domain.exception.UserSessionInvalidException;
import com.javacore.spring_api_luvine.user.domain.valueObject.PersonName;
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

@DisplayName("UpdateProfileUseCase")
@ExtendWith(MockitoExtension.class)
class UpdateProfileUseCaseTest {

    @Mock private UserRepository userRepository;

    @InjectMocks
    private UpdateProfileUseCase updateProfileUseCase;

    // --- HELPERS ------------------------------------------------------------------

    private static final UUID PUBLIC_ID = UUID.randomUUID();

    private CurrentUser currentUser() {
        return new CurrentUser(PUBLIC_ID);
    }

    // --- EXECUTE ------------------------------------------------------------------

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("deve lançar NoProfileChangesProvidedException quando ambos os campos são nulos")
        void execute_bothFieldsNull_throwsNoProfileChangesProvidedException() {
            UpdateProfileRequest request = new UpdateProfileRequest(null, null);

            assertThatExceptionOfType(NoProfileChangesProvidedException.class)
                    .isThrownBy(() -> updateProfileUseCase.execute(currentUser(), request));

            then(userRepository).should(never()).findByPublicId(any());
        }

        @Test
        @DisplayName("deve lançar UserSessionInvalidException quando usuário não encontrado")
        void execute_userNotFound_throwsUserSessionInvalidException() {
            UpdateProfileRequest request = new UpdateProfileRequest("NovoNome", null);

            given(userRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(UserSessionInvalidException.class)
                    .isThrownBy(() -> updateProfileUseCase.execute(currentUser(), request));
        }

        @Test
        @DisplayName("deve atualizar o primeiro nome quando newFirstName é fornecido")
        void execute_newFirstNameProvided_changesFirstName() {
            User user = mock(User.class);
            UpdateProfileRequest request = new UpdateProfileRequest("NovoNome", null);

            given(userRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(user));

            updateProfileUseCase.execute(currentUser(), request);

            then(user).should().changeFirstName(new PersonName("NovoNome"));
            then(user).should(never()).changeLastName(any());
        }

        @Test
        @DisplayName("deve atualizar o último nome quando newLastName é fornecido")
        void execute_newLastNameProvided_changesLastName() {
            User user = mock(User.class);
            UpdateProfileRequest request = new UpdateProfileRequest(null, "NovoSobrenome");

            given(userRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(user));

            updateProfileUseCase.execute(currentUser(), request);

            then(user).should().changeLastName(new PersonName("NovoSobrenome"));
            then(user).should(never()).changeFirstName(any());
        }

        @Test
        @DisplayName("deve atualizar ambos os nomes quando os dois campos são fornecidos")
        void execute_bothFieldsProvided_changesBothNames() {
            User user = mock(User.class);
            UpdateProfileRequest request = new UpdateProfileRequest("NovoNome", "NovoSobrenome");

            given(userRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(user));

            updateProfileUseCase.execute(currentUser(), request);

            then(user).should().changeFirstName(new PersonName("NovoNome"));
            then(user).should().changeLastName(new PersonName("NovoSobrenome"));
        }

        @Test
        @DisplayName("deve lançar exceção quando newFirstName é inválido")
        void execute_invalidFirstName_throwsException() {
            User user = mock(User.class);
            UpdateProfileRequest request = new UpdateProfileRequest("123invalido", null);

            given(userRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(user));

            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> updateProfileUseCase.execute(currentUser(), request));

            then(user).should(never()).changeFirstName(any());
        }
    }
}