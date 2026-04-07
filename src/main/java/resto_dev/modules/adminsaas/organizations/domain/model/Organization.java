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
    private String planId;
    private String subscriptionStatus;
    private LocalDateTime endDate;
    private Integer userLimit;
    private String primaryColor;
    private String secondaryColor;
    private boolean active;
    private boolean hasInventory;
    private boolean hasPensioners;
    private boolean hasKds;
    private String invitationCode;
    private String yapeQrUrl;
    private String plinQrUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Ephemeral field used to share initial credentials only during creation
    private String initialPassword;
}
