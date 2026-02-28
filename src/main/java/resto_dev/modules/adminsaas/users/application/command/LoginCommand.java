package resto_dev.modules.adminsaas.users.application.command;

/**
 * Command for user login.
 */
public record LoginCommand(
        String email,
        String password) {
}
