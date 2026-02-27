package resto_dev.modules.adminsaas.users.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import lombok.*;
import resto_dev.shared.common.BaseEntity;

/**
 * JPA entity for the users table (public schema).
 * Only stores global identity — roles are in restaurant_members.
 */
@Entity
@Table(name = "users", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserJpaEntity extends BaseEntity {

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Builder.Default
    @Column(name = "super_admin", nullable = false)
    private boolean superAdmin = false;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;
}
