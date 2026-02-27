package resto_dev.modules.adminsaas.users.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.adminsaas.users.domain.User;
import resto_dev.modules.adminsaas.users.ports.out.UserRepositoryPort;
import resto_dev.shared.errors.ApiException;

import java.util.UUID;

/**
 * Use case: Get user profile by ID.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserProfileUseCase {

    private final UserRepositoryPort userRepository;

    public User execute(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));
    }
}
