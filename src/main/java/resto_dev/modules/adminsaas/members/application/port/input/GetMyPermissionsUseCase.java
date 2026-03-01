package resto_dev.modules.adminsaas.members.application.port.input;

import java.util.List;
import java.util.UUID;

public interface GetMyPermissionsUseCase {

    /**
     * Retrieves a list of permission codes (e.g. "CREATE_ORDER")
     * for a given user in a specific organization.
     */
    List<String> execute(UUID organizationId, UUID userId);
}
