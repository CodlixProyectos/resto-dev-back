package resto_dev.modules.adminsaas.organizations.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Command to create a new organization (tenant).
 */
public record CreateOrganizationCommand(

        @NotBlank(message = "Organization name is required") @Size(min = 2, max = 100) String name,

        @NotBlank(message = "Slug is required") @Size(min = 2, max = 50) String slug,

        @NotBlank(message = "Owner email is required") String ownerEmail,

        @NotBlank(message = "Owner name is required") String ownerName,

        @Size(max = 50) String type) {
}
