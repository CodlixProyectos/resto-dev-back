package resto_dev.modules.menu.publicmenu.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicProductDTO {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
}
