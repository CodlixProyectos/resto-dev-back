package resto_dev.modules.adminsaas.users.application.port.input;

import resto_dev.modules.adminsaas.users.domain.model.User;
import java.util.UUID;

public interface UpdateUserProfileUseCase {
    User updateUserProfile(UUID userId, String fullName, String phoneNumber, String avatarUrl);
    User updateUserPreferences(UUID userId, boolean notificationsEnabled, boolean soundEnabled, boolean darkModeEnabled);
}
