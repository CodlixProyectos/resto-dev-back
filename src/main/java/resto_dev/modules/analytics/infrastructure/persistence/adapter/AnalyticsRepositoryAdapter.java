package resto_dev.modules.analytics.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.repository.OrganizationJpaRepository;
import resto_dev.modules.analytics.application.port.output.AnalyticsRepositoryPort;
import resto_dev.modules.analytics.domain.model.CategorySales;
import resto_dev.modules.analytics.domain.model.RecentActivity;
import resto_dev.modules.analytics.domain.model.SalesSummary;
import resto_dev.modules.analytics.domain.model.TopSellingProduct;
import resto_dev.modules.analytics.domain.model.DailyRevenue;
import resto_dev.shared.model.DateRange;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AnalyticsRepositoryAdapter implements AnalyticsRepositoryPort {

  private final JdbcTemplate jdbcTemplate;
  private final OrganizationJpaRepository organizationRepository;

  private void setTenantSchema(UUID organizationId) {
    organizationRepository.findById(organizationId).ifPresent(org -> {
      String schema = org.getSchemaName();
      jdbcTemplate.execute("SET SCHEMA '" + schema + "'");
    });
  }

  @Override
  public SalesSummary getSalesSummary(UUID organizationId, DateRange dateRange) {
    setTenantSchema(organizationId);
    LocalDateTime start = dateRange.getStartDate();
    LocalDateTime end = dateRange.getEndDate();

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

    String activeTablesSql = """
        SELECT COUNT(id)
        FROM restaurant_order
        WHERE status NOT IN ('PAID', 'CANCELLED')
        """;

    Integer activeTables = jdbcTemplate.queryForObject(activeTablesSql, Integer.class);

    return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new SalesSummary(
        start.toLocalDate(),
        rs.getInt("total_orders"),
        rs.getBigDecimal("total_revenue"),
        rs.getBigDecimal("avg_ticket"),
        activeTables != null ? activeTables : 0), start, end);
  }

  @Override
  public List<TopSellingProduct> getTopSellingProducts(UUID organizationId, DateRange dateRange, int limit, int offset) {
    setTenantSchema(organizationId);
    LocalDateTime start = dateRange.getStartDate();
    LocalDateTime end = dateRange.getEndDate();

    String sql = """
        SELECT
            product_id,
            product_name,
            SUM(quantity_sold) as quantity_sold,
            SUM(total_revenue) as total_revenue
        FROM (
            SELECT
                oi.product_id,
                oi.product_name,
                SUM(oi.quantity) as quantity_sold,
                SUM(oi.subtotal) as total_revenue
            FROM restaurant_order_item oi
            JOIN restaurant_order o ON oi.order_id = o.id
            WHERE o.status = 'PAID'
              AND o.created_at >= ?
              AND o.created_at <= ?
            GROUP BY oi.product_id, oi.product_name
        ) as sub
        GROUP BY product_id, product_name
        ORDER BY SUM(quantity_sold) DESC
        LIMIT ? OFFSET ?
        """;

    return jdbcTemplate.query(sql, (rs, rowNum) -> new TopSellingProduct(
        UUID.fromString(rs.getString("product_id")),
        rs.getString("product_name"),
        rs.getInt("quantity_sold"),
        rs.getBigDecimal("total_revenue")), start, end, limit, offset);
  }

  @Override
  public List<RecentActivity> getRecentActivity(UUID organizationId, DateRange dateRange, int limit, int offset) {
    setTenantSchema(organizationId);
    LocalDateTime start = dateRange.getStartDate();
    LocalDateTime end = dateRange.getEndDate();

    String sql = """
        SELECT
            o.id,
            o.status,
            o.total,
            o.created_at,
            t.table_number as table_name
        FROM restaurant_order o
        LEFT JOIN restaurant_table t ON o.table_id = t.id
        WHERE o.created_at >= ?
          AND o.created_at <= ?
        ORDER BY o.created_at DESC
        LIMIT ? OFFSET ?
        """;

    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      String status = rs.getString("status");
      String tableName = rs.getString("table_name");
      String title = status.equals("PAID") ? "Pago Recibido" : "Nueva Orden";
      if (tableName != null) {
        title += ": " + tableName;
      }

      String subtitle = "Monto: $" + rs.getBigDecimal("total");
      String icon = status.equals("PAID") ? "DollarSign" : "ShoppingCart";

      return new RecentActivity(
          rs.getString("id"),
          title,
          subtitle,
          rs.getTimestamp("created_at").toLocalDateTime(),
          icon,
          rs.getString("status").toLowerCase());
    }, start, end, limit, offset);
  }

  @Override
  public List<CategorySales> getSalesByCategory(UUID organizationId, DateRange dateRange) {
    setTenantSchema(organizationId);
    LocalDateTime start = dateRange.getStartDate();
    LocalDateTime end = dateRange.getEndDate();

    String sql = """
        SELECT
            c.name as category_name,
            COUNT(DISTINCT o.id) as total_orders,
            SUM(oi.subtotal) as total_revenue
        FROM restaurant_order o
        JOIN restaurant_order_item oi ON o.id = oi.order_id
        JOIN product p ON oi.product_id = p.id
        JOIN category c ON p.category_id = c.id
        WHERE o.status = 'PAID'
          AND o.created_at >= ?
          AND o.created_at <= ?
        GROUP BY c.name
        ORDER BY total_revenue DESC
        """;

    List<CategorySales> results = jdbcTemplate.query(sql, (rs, rowNum) -> new CategorySales(
        rs.getString("category_name"),
        rs.getLong("total_orders"),
        rs.getBigDecimal("total_revenue"),
        0.0 // Percentage will be calculated below
    ), start, end);

    // Calculate percentages
    java.math.BigDecimal grandTotal = results.stream()
        .map(CategorySales::totalRevenue)
        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

    if (grandTotal.compareTo(java.math.BigDecimal.ZERO) > 0) {
      return results.stream()
          .map(cs -> new CategorySales(
              cs.categoryName(),
              cs.totalOrders(),
              cs.totalRevenue(),
              cs.totalRevenue()
                  .multiply(new java.math.BigDecimal(100))
                  .divide(grandTotal, 2, java.math.RoundingMode.HALF_UP)
                  .doubleValue()))
          .toList();
    }

    return results;
  }

  @Override
  public List<DailyRevenue> getRevenueHistory(UUID organizationId, DateRange dateRange) {
    setTenantSchema(organizationId);
    LocalDateTime start = dateRange.getStartDate();
    LocalDateTime end = dateRange.getEndDate();

    String sql = """
        SELECT
            CAST(created_at AS DATE) as revenue_date,
            SUM(total) as daily_revenue
        FROM restaurant_order
        WHERE status = 'PAID'
          AND created_at >= ?
          AND created_at <= ?
        GROUP BY CAST(created_at AS DATE)
        ORDER BY CAST(created_at AS DATE) ASC
        """;

    return jdbcTemplate.query(sql, (rs, rowNum) -> new DailyRevenue(
        rs.getDate("revenue_date").toLocalDate(),
        rs.getBigDecimal("daily_revenue")
    ), start, end);
  }
}
