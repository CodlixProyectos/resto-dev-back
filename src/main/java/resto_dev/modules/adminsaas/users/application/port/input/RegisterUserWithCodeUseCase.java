package resto_dev.modules.adminsaas.users.application.port.input;

import resto_dev.modules.adminsaas.users.application.command.RegisterWithCodeCommand;
import resto_dev.modules.adminsaas.users.domain.model.User;

/**
 * Port for the register with code use case.
 */
public interface RegisterUserWithCodeUseCase {
    User execute(RegisterWithCodeCommand command);
}
