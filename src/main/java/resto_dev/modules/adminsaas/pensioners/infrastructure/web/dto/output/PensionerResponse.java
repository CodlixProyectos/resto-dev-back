package resto_dev.modules.adminsaas.pensioners.infrastructure.web.dto.output;

import java.time.LocalDateTime;
import java.util.UUID;

public record PensionerResponse(
    UUID id,
    UUID organizationId,
    String fullName,
    String dni,
    String email,
    String phoneNumber,
    boolean active,
    LocalDateTime createdAt
) {}
