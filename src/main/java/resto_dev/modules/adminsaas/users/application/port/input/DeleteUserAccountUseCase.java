package resto_dev.modules.adminsaas.users.application.port.input;

import java.util.UUID;

public interface DeleteUserAccountUseCase {
    void deleteUserAccount(UUID userId);
}
