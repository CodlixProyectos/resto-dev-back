package resto_dev.modules.layout.zones.application.port.input;

import resto_dev.modules.layout.zones.application.command.CreateZoneCommand;
import resto_dev.modules.layout.zones.domain.model.Zone;

public interface CreateZoneUseCase {
    Zone execute(CreateZoneCommand command);
}
