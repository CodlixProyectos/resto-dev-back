package resto_dev.modules.adminsaas.organizations.domain.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Organization domain entity — represents a tenant in the SaaS.
 * Generic: could be a restaurant, hotel, gym, clinic, etc.
 * Each organization gets its own PostgreSQL schema: client_{uuid}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organization {

    private UUID id;
    private String name;
    private String slug;
    private String schemaName;
    private String type;
    private UUID ownerId;
    private String legalName;
    private String businessId;
    private String email;
    private String phone;
    private String address;
    private String logoUrl;
    private String sunatUser;
    private String sunatPassword;
    private String sunatClientId;
    private String sunatClientSecret;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
