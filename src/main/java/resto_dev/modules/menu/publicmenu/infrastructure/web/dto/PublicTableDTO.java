package resto_dev.modules.menu.publicmenu.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicTableDTO {
    private UUID id;
    private String name;
    private int capacity;
    private UUID zoneId;
    private String zoneName;
}
