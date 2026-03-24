package resto_dev.modules.layout.zones.application.query;

import lombok.Builder;

@Builder
public record SearchZonesQuery(
        String search,
        Boolean isActive,
        int page,
        int size) {
}
