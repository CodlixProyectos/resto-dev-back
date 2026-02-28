package resto_dev.modules.adminsaas.users.application.command;

/**
 * Command for user registration.
 */
public record RegisterCommand(
                String email,
                String password,
                String fullName) {
}
