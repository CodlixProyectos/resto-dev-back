package resto_dev.modules.adminsaas.members.application.port.input;

import resto_dev.modules.adminsaas.users.application.command.AuthResult;
import java.util.UUID;

public interface PinLoginOrganizationMemberUseCase {
    AuthResult execute(UUID organizationId, String email, String dni, String pin);
}
