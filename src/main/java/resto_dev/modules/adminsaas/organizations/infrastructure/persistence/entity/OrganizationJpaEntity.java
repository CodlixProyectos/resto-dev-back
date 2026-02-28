package resto_dev.modules.adminsaas.organizations.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import resto_dev.shared.common.BaseEntity;

import java.util.UUID;

/**
 * JPA entity for the organizations table (public schema).
 * Generic tenant — can be a restaurant, hotel, gym, etc.
 */
@Entity
@Table(name = "organizations", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationJpaEntity extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slug", unique = true, nullable = false, length = 60)
    private String slug;

    @Column(name = "schema_name", unique = true, nullable = false, length = 30)
    private String schemaName;

    @Builder.Default
    @Column(name = "type", nullable = false, length = 50)
    private String type = "restaurant";

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;
}
