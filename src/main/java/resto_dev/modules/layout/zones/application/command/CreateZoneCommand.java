package resto_dev.modules.layout.zones.application.command;

import lombok.Builder;

@Builder
public record CreateZoneCommand(
        String name,
        String description) {
}
