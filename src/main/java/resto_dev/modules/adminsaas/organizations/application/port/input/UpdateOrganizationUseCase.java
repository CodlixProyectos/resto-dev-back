package resto_dev.modules.adminsaas.organizations.application.port.input;

import resto_dev.modules.adminsaas.organizations.domain.model.Organization;
import java.util.UUID;

public interface UpdateOrganizationUseCase {
    Organization execute(UUID id, UpdateOrganizationCommand command);

    record UpdateOrganizationCommand(
            String name,
            String legalName,
            String businessId,
            String email,
            String phone,
            String address,
            String logoUrl,
            String primaryColor,
            String secondaryColor,
            String sunatUser,
            String sunatPassword,
            String sunatClientId,
            String sunatClientSecret) {
    }
}
