package resto_dev.modules.adminsaas.users.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.adminsaas.users.domain.User;
import resto_dev.modules.adminsaas.users.ports.in.RegisterUserPort;
import resto_dev.modules.adminsaas.users.ports.in.dto.RegisterCommand;
import resto_dev.modules.adminsaas.users.ports.out.UserRepositoryPort;
import resto_dev.shared.errors.ApiException;

/**
 * Use case: Register a new user (global identity only).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RegisterUserUseCase implements RegisterUserPort {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User execute(RegisterCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw ApiException.conflict("Email already registered: " + command.email());
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
