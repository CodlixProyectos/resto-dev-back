package resto_dev.modules.adminsaas.pensioners.application.port.input;

import resto_dev.modules.adminsaas.pensioners.domain.model.Pensioner;

import java.util.UUID;

public interface AddPensionerUseCase {
    Pensioner execute(UUID organizationId, String fullName, String dni, String email, String phoneNumber);
}
