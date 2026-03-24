package resto_dev.modules.adminsaas.pensioners.application.port.input;

import java.util.UUID;

public interface DeleteConsumptionUseCase {
    void execute(UUID consumptionId);
}
