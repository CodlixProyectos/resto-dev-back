package resto_dev.modules.adminsaas.users.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.adminsaas.users.domain.User;
import resto_dev.modules.adminsaas.users.ports.in.LoginUserPort;
import resto_dev.modules.adminsaas.users.ports.in.dto.AuthResult;
import resto_dev.modules.adminsaas.users.ports.in.dto.LoginCommand;
import resto_dev.modules.adminsaas.users.ports.out.UserRepositoryPort;
import resto_dev.shared.errors.ApiException;
import resto_dev.shared.security.jwt.JwtProvider;

/**
 * Use case: Login — validates credentials and generates JWT.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginUserUseCase implements LoginUserPort {

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
                user.getEmail(),
                user.getFullName(),
                user.isSuperAdmin());
    }
}
