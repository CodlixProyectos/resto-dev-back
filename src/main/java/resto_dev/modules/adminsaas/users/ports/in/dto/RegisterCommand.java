package resto_dev.modules.adminsaas.users.ports.in.dto;

/**
 * Command for user registration.
 */
public record RegisterCommand(
                String email,
                String password,
                String fullName) {
}
