package resto_dev.modules.adminsaas.members.infrastructure.web.dto.output;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrganizationMemberResponse(
    UUID memberId,
    UUID organizationId,
    UUID userId,
    UUID roleId,
    String fullName,
    String email,
    String phoneNumber,
    String dni,
    String roleName,
    boolean isActive,
    String status,
    BigDecimal salary,
    LocalDateTime joinedAt
) {
}
