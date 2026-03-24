package resto_dev.modules.layout.zones.application.command;

import lombok.Builder;

@Builder
public record UpdateZoneCommand(
        String name,
        String description,
        boolean active,
        Double entrancePosX,
        Double entrancePosY) {
}
