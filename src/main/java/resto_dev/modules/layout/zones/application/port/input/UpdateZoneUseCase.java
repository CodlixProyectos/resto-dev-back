package resto_dev.modules.layout.zones.application.port.input;

import resto_dev.modules.layout.zones.application.command.UpdateZoneCommand;
import resto_dev.modules.layout.zones.domain.model.Zone;

import java.util.UUID;

public interface UpdateZoneUseCase {
    Zone execute(UUID id, UpdateZoneCommand command);
}
