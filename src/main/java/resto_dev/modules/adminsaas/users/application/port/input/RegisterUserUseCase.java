package resto_dev.modules.adminsaas.users.application.port.input;

import resto_dev.modules.adminsaas.users.domain.model.User;
import resto_dev.modules.adminsaas.users.application.command.RegisterCommand;

/**
 * Input port for user registration use case.
 */
public interface RegisterUserUseCase {

    User execute(RegisterCommand command);
}
