package resto_dev.modules.adminsaas.users.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.adminsaas.users.application.port.input.DeleteUserAccountUseCase;
import resto_dev.modules.adminsaas.users.application.port.input.UpdateUserPasswordUseCase;
import resto_dev.modules.adminsaas.users.application.port.input.UpdateUserProfileUseCase;
import resto_dev.modules.adminsaas.users.application.port.output.UserRepositoryPort;
import resto_dev.modules.adminsaas.users.domain.model.User;
import resto_dev.shared.errors.ApiException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManageUserProfileApplicationService
        implements UpdateUserProfileUseCase, UpdateUserPasswordUseCase, DeleteUserAccountUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User updateUserProfile(UUID userId, String fullName, String phoneNumber, String avatarUrl) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));

        user.setFullName(fullName);
        user.setPhoneNumber(phoneNumber);
        user.setAvatarUrl(avatarUrl);

        return userRepositoryPort.save(user);
    }

    @Override
    @Transactional
    public void updateUserPassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw ApiException.unauthorized("La contraseña actual es incorrecta");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepositoryPort.save(user);
    }

    @Override
    @Transactional
    public void deleteUserAccount(UUID userId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));

        userRepositoryPort.deleteById(user.getId());
    }
}
