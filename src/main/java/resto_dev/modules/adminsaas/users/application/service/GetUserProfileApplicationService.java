package resto_dev.modules.adminsaas.users.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.adminsaas.users.domain.model.User;
import resto_dev.modules.adminsaas.users.application.port.output.UserRepositoryPort;
import resto_dev.shared.errors.ApiException;

import java.util.UUID;

/**
 * Use case: Get user profile by ID.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserProfileApplicationService {

    private final UserRepositoryPort userRepository;

    public User execute(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));
    }
}
