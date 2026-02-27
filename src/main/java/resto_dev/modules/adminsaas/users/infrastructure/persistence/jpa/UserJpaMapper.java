package resto_dev.modules.adminsaas.users.infrastructure.persistence.jpa;

import org.springframework.stereotype.Component;
import resto_dev.modules.adminsaas.users.domain.User;

/**
 * Maps between domain User and JPA UserJpaEntity.
 * Keeps JPA annotations out of the domain layer.
 */
@Component
public class UserJpaMapper {

    public User toDomain(UserJpaEntity entity) {
        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .fullName(entity.getFullName())
                .role(entity.getRole())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = UserJpaEntity.builder()
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .fullName(user.getFullName())
                .role(user.getRole())
                .active(user.isActive())
                .build();

        // Preserve ID for updates
        if (user.getId() != null) {
            entity.setId(user.getId());
        }

        return entity;
    }
}
