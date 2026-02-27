package resto_dev.modules.adminsaas.users.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import lombok.*;
import resto_dev.modules.adminsaas.users.domain.Role;
import resto_dev.shared.common.BaseEntity;

/**
 * JPA entity for the users table (public schema).
 * Separate from the domain User to keep JPA concerns out of the domain.
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

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;
}
