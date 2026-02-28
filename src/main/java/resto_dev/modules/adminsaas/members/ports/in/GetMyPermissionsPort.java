package resto_dev.modules.adminsaas.members.ports.in;

import java.util.List;
import java.util.UUID;

public interface GetMyPermissionsPort {

    /**
     * Retrieves a list of permission codes (e.g. "CREATE_ORDER")
     * for a given user in a specific organization.
     */
    List<String> execute(UUID organizationId, UUID userId);
}
