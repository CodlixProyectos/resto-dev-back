package resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateOrganizationRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 255) String legalName,
        @Size(max = 50) String businessId,
        @Size(max = 255) String email,
        @Size(max = 50) String phone,
        String address,
        String logoUrl
) {
}
