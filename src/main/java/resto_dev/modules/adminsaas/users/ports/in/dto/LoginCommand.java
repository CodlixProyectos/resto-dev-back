package resto_dev.modules.adminsaas.users.ports.in.dto;

/**
 * Command for user login.
 */
public record LoginCommand(
        String email,
        String password) {
}
