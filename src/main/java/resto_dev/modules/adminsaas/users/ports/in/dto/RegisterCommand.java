package resto_dev.modules.adminsaas.users.ports.in.dto;

import resto_dev.modules.adminsaas.users.domain.Role;

/**
 * Command for user registration.
 */
public record RegisterCommand(
        String email,
        String password,
        String fullName,
        Role role) {
}
