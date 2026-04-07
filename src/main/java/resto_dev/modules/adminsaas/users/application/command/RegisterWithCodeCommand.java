package resto_dev.modules.adminsaas.users.application.command;

/**
 * Command for registering a user with an invitation code.
 */
public record RegisterWithCodeCommand(
    String fullName,
    String email,
    String password,
    String invitationCode,
    String organizationName
) {
}
