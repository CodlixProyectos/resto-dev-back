package resto_dev.modules.adminsaas.organizations.ports.in;

import resto_dev.modules.adminsaas.organizations.domain.Organization;
import resto_dev.modules.adminsaas.organizations.ports.in.dto.CreateOrganizationCommand;

import java.util.UUID;

/**
 * Input port for creating an organization.
 */
public interface CreateOrganizationPort {

    Organization execute(CreateOrganizationCommand command, UUID ownerId);
}
