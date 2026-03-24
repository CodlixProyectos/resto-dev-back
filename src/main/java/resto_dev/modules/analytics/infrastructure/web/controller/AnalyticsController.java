package resto_dev.modules.analytics.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.analytics.application.port.input.GetRecentActivityUseCase;
import resto_dev.modules.analytics.application.port.input.GetSalesByCategoryUseCase;
import resto_dev.modules.analytics.application.port.input.GetSalesSummaryUseCase;
import resto_dev.modules.analytics.application.port.input.GetTopProductsUseCase;
import resto_dev.modules.analytics.domain.model.CategorySales;
import resto_dev.modules.analytics.domain.model.RecentActivity;
import resto_dev.modules.analytics.domain.model.SalesSummary;
import resto_dev.modules.analytics.domain.model.TopSellingProduct;
import resto_dev.shared.model.DateRange;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Endpoints for restaurant analytics and dashboard data")
public class AnalyticsController {

    private final GetSalesSummaryUseCase getSalesSummaryUseCase;
    private final GetTopProductsUseCase getTopProductsUseCase;
    private final GetRecentActivityUseCase getRecentActivityUseCase;
    private final GetSalesByCategoryUseCase getSalesByCategoryUseCase;

    @GetMapping("/sales-summary")
    @Operation(summary = "Get sales summary", description = "Returns total revenue, order count, and other KPIs for a date range")
    public ResponseEntity<SalesSummary> getSalesSummary(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {

        DateRange dateRange = toDateRange(startDate, endDate);
        SalesSummary summary = getSalesSummaryUseCase.execute(orgId, dateRange);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/top-products")
    @Operation(summary = "Get top selling products", description = "Returns products with most sales in a date range")
    public ResponseEntity<List<TopSellingProduct>> getTopProducts(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @RequestParam(defaultValue = "15") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        DateRange dateRange = toDateRange(startDate, endDate);
        List<TopSellingProduct> products = getTopProductsUseCase.execute(orgId, dateRange, limit, offset);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/recent-activity")
    @Operation(summary = "Get recent activity", description = "Returns the latest events in the restaurant (new orders, payments, etc.)")
    public ResponseEntity<List<RecentActivity>> getRecentActivity(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @RequestParam(defaultValue = "15") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        DateRange dateRange = toDateRange(startDate, endDate);
        List<RecentActivity> activities = getRecentActivityUseCase.execute(orgId, dateRange, limit, offset);
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/sales-by-category")
    @Operation(summary = "Get sales by category", description = "Returns sales distribution across menu categories")
    public ResponseEntity<List<CategorySales>> getSalesByCategory(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {

        DateRange dateRange = toDateRange(startDate, endDate);
        List<CategorySales> sales = getSalesByCategoryUseCase.execute(orgId, dateRange);
        return ResponseEntity.ok(sales);
    }

    private DateRange toDateRange(OffsetDateTime start, OffsetDateTime end) {
        LocalDateTime startDate = (start != null) ? start.toLocalDateTime() : null;
        LocalDateTime endDate = (end != null) ? end.toLocalDateTime() : null;
        return DateRange.of(startDate, endDate);
    }
}
