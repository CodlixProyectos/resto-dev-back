package resto_dev.modules.analytics.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAnalytics {
    private long totalItems;
    private long lowStockCount;
    private BigDecimal totalInventoryValue;
    private List<CategoryStock> categoryDistribution;
    private List<CriticalStockItem> criticalItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStock {
        private String categoryName;
        private BigDecimal value;
        private double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CriticalStockItem {
        private String name;
        private BigDecimal currentStock;
        private BigDecimal minStock;
        private String unit;
    }
}
