package resto_dev.modules.adminsaas.users.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import resto_dev.modules.adminsaas.users.domain.model.User;
import resto_dev.modules.adminsaas.users.application.port.output.UserRepositoryPort;
import resto_dev.modules.adminsaas.users.infrastructure.persistence.entity.UserJpaEntity;
import resto_dev.modules.adminsaas.users.infrastructure.persistence.repository.UserJpaRepository;
import resto_dev.modules.adminsaas.users.infrastructure.persistence.mapper.UserJpaMapper;


import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing UserRepositoryPort using Spring Data JPA.
 * This is where the hexagonal architecture connects to the real database.
 */
@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpaRepository;
    private final UserJpaMapper mapper;

    @Override
    public User save(User user) {
        UserJpaEntity entity = mapper.toEntity(user);
        UserJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}
