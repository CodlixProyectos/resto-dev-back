package resto_dev.modules.menu.products.application.query;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SearchProductsQuery {
    private String search;
    private UUID categoryId;
    private Boolean isActive;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;
}
