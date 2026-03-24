package resto_dev.modules.adminsaas.users.infrastructure.web.mapper;

import org.springframework.stereotype.Component;
import resto_dev.modules.adminsaas.users.infrastructure.web.dto.input.RegisterRequest;
import resto_dev.modules.adminsaas.users.infrastructure.web.dto.output.UserResponse;
import resto_dev.modules.adminsaas.users.domain.model.User;
import resto_dev.modules.adminsaas.users.application.command.RegisterCommand;

/**
 * Maps between web DTOs and domain/port objects.
 */
@Component
public class UserWebMapper {

    public RegisterCommand toCommand(RegisterRequest request) {
        return new RegisterCommand(
                request.email(),
                request.password(),
                request.fullName());
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getAvatarUrl(),
                user.isSuperAdmin(),
                user.isActive(),
                user.isNotificationsEnabled(),
                user.isSoundEnabled(),
                user.isDarkModeEnabled());
    }
}
