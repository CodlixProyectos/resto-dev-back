package resto_dev.shared.security.permissions;

import jakarta.persistence.*;
import lombok.*;
import resto_dev.shared.common.BaseEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * Role entity — system and custom roles.
 * Examples: OWNER, MANAGER, WAITER, KITCHEN.
 */
@Entity
@Table(name = "roles", schema = "admin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleEntity extends BaseEntity {

    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name;

    @Column(name = "description")
    private String description;

    @Builder.Default
    @Column(name = "is_system", nullable = false)
    private boolean system = false;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "role_permissions", schema = "admin", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<PermissionEntity> permissions = new HashSet<>();
}
