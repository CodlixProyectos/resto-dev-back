package resto_dev.modules.adminsaas.pensioners.application.port.input;

import resto_dev.modules.adminsaas.pensioners.domain.model.Pensioner;

import java.util.UUID;

public interface UpdatePensionerUseCase {
    Pensioner execute(UUID organizationId, UUID pensionerId, String fullName, String dni, String email, String phoneNumber, boolean active);
}
