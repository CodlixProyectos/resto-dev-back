package resto_dev.modules.layout.zones.infrastructure.web.mapper;

import org.springframework.stereotype.Component;
import resto_dev.modules.layout.zones.application.command.CreateZoneCommand;
import resto_dev.modules.layout.zones.application.command.UpdateZoneCommand;
import resto_dev.modules.layout.zones.domain.model.Zone;
import resto_dev.modules.layout.zones.infrastructure.web.dto.input.CreateZoneRequest;
import resto_dev.modules.layout.zones.infrastructure.web.dto.input.UpdateZoneRequest;
import resto_dev.modules.layout.zones.infrastructure.web.dto.output.ZoneResponse;

@Component
public class ZoneWebMapper {

    public CreateZoneCommand toCommand(CreateZoneRequest request) {
        return CreateZoneCommand.builder()
                .name(request.name())
                .description(request.description())
                .build();
    }

    public UpdateZoneCommand toCommand(UpdateZoneRequest request) {
        return UpdateZoneCommand.builder()
                .name(request.name())
                .description(request.description())
                .active(request.active())
                .entrancePosX(request.entrancePosX())
                .entrancePosY(request.entrancePosY())
                .build();
    }

    public ZoneResponse toResponse(Zone zone) {
        if (zone == null) {
            return null;
        }

        return new ZoneResponse(
                zone.getId(),
                zone.getName(),
                zone.getDescription(),
                zone.isActive(),
                zone.getEntrancePosX(),
                zone.getEntrancePosY());
    }
}
