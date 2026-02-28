package resto_dev.modules.adminsaas.organizations.application.port.input;

import resto_dev.modules.adminsaas.organizations.domain.model.Organization;
import resto_dev.modules.adminsaas.organizations.application.command.CreateOrganizationCommand;

import java.util.UUID;

/**
 * Input port for creating an organization.
 */
public interface CreateOrganizationUseCase {

    Organization execute(CreateOrganizationCommand command, UUID ownerId);
}
