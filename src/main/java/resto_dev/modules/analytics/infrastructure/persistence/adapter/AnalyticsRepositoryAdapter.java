package resto_dev.modules.analytics.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import resto_dev.modules.analytics.application.port.output.AnalyticsRepositoryPort;
import resto_dev.modules.analytics.domain.model.SalesSummary;
import resto_dev.modules.analytics.domain.model.TopSellingProduct;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AnalyticsRepositoryAdapter implements AnalyticsRepositoryPort {

  private final JdbcTemplate jdbcTemplate;

  @Override
  public SalesSummary getSalesSummary(LocalDateTime startDate, LocalDateTime endDate) {
    String sql = """
        SELECT
            COUNT(id) as total_orders,
            COALESCE(SUM(total), 0) as total_revenue,
            COALESCE(AVG(total), 0) as avg_ticket
        FROM restaurant_order
        WHERE status = 'PAID'
          AND created_at >= ?
          AND created_at <= ?
        """;

    return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new SalesSummary(
        startDate.toLocalDate(),
        rs.getInt("total_orders"),
        rs.getBigDecimal("total_revenue"),
        rs.getBigDecimal("avg_ticket")), startDate, endDate);
  }

  @Override
  public List<TopSellingProduct> getTopSellingProducts(LocalDateTime startDate, LocalDateTime endDate, int limit) {
    String sql = """
        SELECT
            product_id,
            product_name,
            SUM(quantity) as quantity_sold,
            SUM(subtotal) as total_revenue
        FROM restaurant_order_item
        WHERE status = 'PAID'
          AND created_at >= ?
          AND created_at <= ?
        GROUP BY product_id, product_name
        ORDER BY quantity_sold DESC
        LIMIT ?
        """;

    return jdbcTemplate.query(sql, (rs, rowNum) -> new TopSellingProduct(
        UUID.fromString(rs.getString("product_id")),
        rs.getString("product_name"),
        rs.getInt("quantity_sold"),
        rs.getBigDecimal("total_revenue")), startDate, endDate, limit);
  }
}
