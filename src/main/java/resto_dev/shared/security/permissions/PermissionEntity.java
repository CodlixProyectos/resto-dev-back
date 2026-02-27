package resto_dev.shared.security.permissions;

import jakarta.persistence.*;
import lombok.*;
import resto_dev.shared.common.BaseEntity;

/**
 * Permission entity — granular access control.
 * Examples: CREATE_ORDER, VIEW_REPORTS, MANAGE_MENU.
 */
@Entity
@Table(name = "permissions", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionEntity extends BaseEntity {

    @Column(name = "code", unique = true, nullable = false, length = 80)
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "module", nullable = false, length = 50)
    private String module;
}
