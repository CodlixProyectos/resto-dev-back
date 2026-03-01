package resto_dev.modules.adminsaas.members.domain.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity: links a User to an Organization with a specific Role.
 * Generic pivot — works for any organization type (restaurant, hotel, etc.).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationMember {

    private UUID id;
    private UUID organizationId;
    private UUID userId;
    private UUID roleId;
    private String pin;
    private boolean active;
    private LocalDateTime joinedAt;
}
