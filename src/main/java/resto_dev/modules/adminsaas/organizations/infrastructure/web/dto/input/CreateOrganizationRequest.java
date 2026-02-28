package resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Web request DTO to create a new organization.
 */
public record CreateOrganizationRequest(

        @NotBlank(message = "Organization name is required") @Size(min = 2, max = 100) String name,

        @NotBlank(message = "Slug is required") @Size(min = 2, max = 50) String slug,

        @Size(max = 50) String type) {
}
