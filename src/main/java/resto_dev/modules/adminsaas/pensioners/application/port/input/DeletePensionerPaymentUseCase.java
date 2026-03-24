package resto_dev.modules.adminsaas.pensioners.application.port.input;

import java.util.UUID;

public interface DeletePensionerPaymentUseCase {
    void execute(UUID id);
}
