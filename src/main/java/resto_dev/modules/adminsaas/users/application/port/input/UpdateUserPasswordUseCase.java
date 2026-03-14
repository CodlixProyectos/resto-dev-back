package resto_dev.modules.adminsaas.users.application.port.input;

import java.util.UUID;

public interface UpdateUserPasswordUseCase {
    void updateUserPassword(UUID userId, String currentPassword, String newPassword);
}
