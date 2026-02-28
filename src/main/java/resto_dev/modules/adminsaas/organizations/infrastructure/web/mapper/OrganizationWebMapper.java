package resto_dev.modules.adminsaas.organizations.infrastructure.web.mapper;

import org.springframework.stereotype.Component;
import resto_dev.modules.adminsaas.organizations.application.command.CreateOrganizationCommand;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.input.CreateOrganizationRequest;

/**
 * Maps Web DTOs to Application Commands for Organizations.
 */
@Component
public class OrganizationWebMapper {

    public CreateOrganizationCommand toCommand(CreateOrganizationRequest request) {
        return new CreateOrganizationCommand(
                request.name(),
                request.slug(),
                request.type());
    }
}
