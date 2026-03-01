package resto_dev.modules.layout.zones.application.port.input;

import java.util.UUID;

public interface DeleteZoneUseCase {
    void execute(UUID id);
}
