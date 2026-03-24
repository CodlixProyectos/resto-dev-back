package resto_dev.modules.adminsaas.users.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.adminsaas.users.domain.model.User;
import resto_dev.modules.adminsaas.users.application.port.input.LoginUserUseCase;
import resto_dev.modules.adminsaas.users.application.command.AuthResult;
import resto_dev.modules.adminsaas.users.application.command.LoginCommand;
import resto_dev.modules.adminsaas.users.application.port.output.UserRepositoryPort;
import resto_dev.shared.errors.ApiException;
import resto_dev.shared.security.jwt.JwtProvider;

/**
 * Use case: Login — validates credentials and generates JWT.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginUserApplicationService implements LoginUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    public AuthResult execute(LoginCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Invalid email or password");
        }

        if (!user.isActive()) {
            throw ApiException.forbidden("Account is disabled");
        }

        String token = jwtProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.isSuperAdmin());

        return new AuthResult(
                token,
                user.getId(),
                null, // memberId - global login doesn't focus on a single organization member
                null, // organizationId
                user.getEmail(),
                user.getFullName(),
                user.isSuperAdmin(),
                "ADMIN", // Default role for global admin login
                user.isNotificationsEnabled(),
                user.isSoundEnabled(),
                user.isDarkModeEnabled());
    }
}
