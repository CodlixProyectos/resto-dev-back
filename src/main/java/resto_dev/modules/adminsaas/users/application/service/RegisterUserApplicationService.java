package resto_dev.modules.adminsaas.users.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.adminsaas.users.domain.model.User;
import resto_dev.modules.adminsaas.users.application.port.input.RegisterUserUseCase;
import resto_dev.modules.adminsaas.users.application.command.RegisterCommand;
import resto_dev.modules.adminsaas.users.application.port.output.UserRepositoryPort;
import resto_dev.shared.errors.ApiException;

/**
 * Use case: Register a new user (global identity only).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RegisterUserApplicationService implements RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User execute(RegisterCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw ApiException.conflict("El correo electrónico ya se encuentra registrado: " + command.email());
        }

        User user = User.builder()
                .email(command.email())
                .passwordHash(passwordEncoder.encode(command.password()))
                .fullName(command.fullName())
                .superAdmin(false)
                .active(true)
                .build();

        return userRepository.save(user);
    }
}
