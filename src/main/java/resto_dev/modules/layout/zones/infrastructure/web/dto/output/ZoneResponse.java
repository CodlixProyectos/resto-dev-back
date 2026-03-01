package resto_dev.modules.layout.zones.infrastructure.web.dto.output;

import java.util.UUID;

public record ZoneResponse(
        UUID id,
        String name,
        String description,
        boolean active) {
}
