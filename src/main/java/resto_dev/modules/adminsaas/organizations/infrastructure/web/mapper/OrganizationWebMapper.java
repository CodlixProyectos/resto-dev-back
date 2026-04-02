package resto_dev.modules.adminsaas.organizations.infrastructure.web.mapper;

import org.springframework.stereotype.Component;
import resto_dev.modules.adminsaas.organizations.application.command.CreateOrganizationCommand;
import resto_dev.modules.adminsaas.organizations.domain.model.Organization;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.input.CreateOrganizationRequest;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.output.OrganizationResponse;

/**
 * Maps Web DTOs to Application Commands and Domain to Web Responses for
 * Organizations.
 */
@Component
public class OrganizationWebMapper {

    public CreateOrganizationCommand toCommand(CreateOrganizationRequest request) {
        return new CreateOrganizationCommand(
                request.name(),
                request.slug(),
                request.type());
    }

    public OrganizationResponse toResponse(Organization o) {
        if (o == null) {
            return null;
        }
        return new OrganizationResponse(
                o.getId(),
                o.getName(),
                o.getSlug(),
                o.getSchemaName(),
                o.getType(),
                o.getOwnerId(),
                o.isActive(),
                o.getLegalName(),
                o.getBusinessId(),
                o.getEmail(),
                o.getPhone(),
                o.getAddress(),
                o.getLogoUrl(),
                o.getPrimaryColor(),
                o.getSecondaryColor(),
                o.getSunatUser(),
                o.getSunatPassword(),
                o.getSunatClientId(),
                o.getSunatClientSecret());
    }
}
