package resto_dev.modules.adminsaas.users.infrastructure.persistence.mapper;

import resto_dev.modules.adminsaas.users.infrastructure.persistence.entity.UserJpaEntity;
import resto_dev.modules.adminsaas.users.domain.model.User;

import org.springframework.stereotype.Component;

/**
 * Maps between domain User and JPA UserJpaEntity.
 */
@Component
public class UserJpaMapper {

    public User toDomain(UserJpaEntity entity) {
        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .fullName(entity.getFullName())
                .phoneNumber(entity.getPhoneNumber())
                .dni(entity.getDni())
                .avatarUrl(entity.getAvatarUrl())
                .superAdmin(entity.isSuperAdmin())
                .active(entity.isActive())
                .notificationsEnabled(entity.isNotificationsEnabled())
                .soundEnabled(entity.isSoundEnabled())
                .darkModeEnabled(entity.isDarkModeEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = UserJpaEntity.builder()
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .dni(user.getDni())
                .avatarUrl(user.getAvatarUrl())
                .superAdmin(user.isSuperAdmin())
                .active(user.isActive())
                .notificationsEnabled(user.isNotificationsEnabled())
                .soundEnabled(user.isSoundEnabled())
                .darkModeEnabled(user.isDarkModeEnabled())
                .build();

        if (user.getId() != null) {
            entity.setId(user.getId());
        }

        return entity;
    }
}
