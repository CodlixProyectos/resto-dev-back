package resto_dev.modules.pensioners.application.port.input;

import java.util.UUID;

public interface DeleteConsumptionUseCase {
    void execute(UUID consumptionId);
}
