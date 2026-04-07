package resto_dev.modules.pensioners.application.port.input;

import java.util.UUID;

public interface DeletePensionerPaymentUseCase {
    void execute(UUID id);
}
