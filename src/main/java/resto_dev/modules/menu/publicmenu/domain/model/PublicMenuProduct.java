package resto_dev.modules.menu.publicmenu.domain.model;

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
public class PublicMenuProduct {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
}
