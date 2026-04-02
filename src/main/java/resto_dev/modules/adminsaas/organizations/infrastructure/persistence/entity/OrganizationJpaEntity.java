package resto_dev.modules.adminsaas.organizations.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import resto_dev.shared.common.BaseEntity;
import resto_dev.shared.security.AttributeEncryptionConverter;

import java.util.UUID;

/**
 * JPA entity for the organizations table (admin schema).
 * Generic tenant — can be a restaurant, hotel, gym, etc.
 */
@Entity
@Table(name = "organizations", schema = "admin")
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

    @Column(name = "legal_name")
    private String legalName;

    @Column(name = "business_id", length = 50)
    private String businessId;

    @Column(name = "sunat_user", length = 100)
    private String sunatUser;

    @Column(name = "sunat_password")
    @Convert(converter = AttributeEncryptionConverter.class)
    private String sunatPassword;
    
    @Column(name = "sunat_client_id", length = 100)
    private String sunatClientId;

    @Column(name = "sunat_client_secret")
    @Convert(converter = AttributeEncryptionConverter.class)
    private String sunatClientSecret;

    @Column(name = "email")
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;

    @Column(name = "primary_color", length = 20)
    private String primaryColor;

    @Column(name = "secondary_color", length = 20)
    private String secondaryColor;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;
}
