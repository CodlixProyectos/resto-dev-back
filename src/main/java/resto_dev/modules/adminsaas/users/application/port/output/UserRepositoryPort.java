package resto_dev.modules.adminsaas.users.application.port.output;

import resto_dev.modules.adminsaas.users.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Output port for user persistence.
 * Infrastructure layer implements this interface (Dependency Inversion).
 */
public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findByDni(String dni);

    Optional<User> findById(UUID id);

    boolean existsByEmail(String email);

    void deleteById(UUID id);
}
