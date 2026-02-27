package resto_dev.modules.adminsaas.users.ports.in;

import resto_dev.modules.adminsaas.users.domain.User;
import resto_dev.modules.adminsaas.users.ports.in.dto.RegisterCommand;

/**
 * Input port for user registration use case.
 */
public interface RegisterUserPort {

    User execute(RegisterCommand command);
}
