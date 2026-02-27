package resto_dev.modules.adminsaas.users.ports.in;

import resto_dev.modules.adminsaas.users.ports.in.dto.AuthResult;
import resto_dev.modules.adminsaas.users.ports.in.dto.LoginCommand;

/**
 * Input port for user login use case.
 */
public interface LoginUserPort {

    AuthResult execute(LoginCommand command);
}
