package resto_dev.modules.adminsaas.users.application.port.input;

import resto_dev.modules.adminsaas.users.application.command.AuthResult;
import resto_dev.modules.adminsaas.users.application.command.LoginCommand;

/**
 * Input port for user login use case.
 */
public interface LoginUserUseCase {

    AuthResult execute(LoginCommand command);
}
