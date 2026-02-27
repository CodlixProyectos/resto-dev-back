package resto_dev.modules.adminsaas.users.adapters.mappers;

import org.springframework.stereotype.Component;
import resto_dev.modules.adminsaas.users.adapters.web.dto.RegisterRequest;
import resto_dev.modules.adminsaas.users.adapters.web.dto.UserResponse;
import resto_dev.modules.adminsaas.users.domain.Role;
import resto_dev.modules.adminsaas.users.domain.User;
import resto_dev.modules.adminsaas.users.ports.in.dto.RegisterCommand;

/**
 * Maps between web DTOs and domain/port objects.
 */
@Component
public class UserDtoMapper {

    public RegisterCommand toCommand(RegisterRequest request) {
        return new RegisterCommand(
                request.email(),
                request.password(),
                request.fullName(),
                Role.valueOf(request.role().toUpperCase()));
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                user.isActive());
    }
}
