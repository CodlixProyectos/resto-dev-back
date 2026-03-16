package resto_dev.modules.analytics.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.analytics.application.port.input.GetRecentActivityUseCase;
import resto_dev.modules.analytics.application.port.input.GetSalesByCategoryUseCase;
import resto_dev.modules.analytics.application.port.input.GetSalesSummaryUseCase;
import resto_dev.modules.analytics.application.port.input.GetTopProductsUseCase;
import resto_dev.modules.analytics.domain.model.CategorySales;
import resto_dev.modules.analytics.domain.model.RecentActivity;
import resto_dev.modules.analytics.domain.model.SalesSummary;
import resto_dev.modules.analytics.domain.model.TopSellingProduct;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Endpoints for restaurant dashboard statistics and reports")
public class AnalyticsController {

    private final GetSalesSummaryUseCase getSalesSummaryUseCase;
    private final GetTopProductsUseCase getTopProductsUseCase;
    private final GetRecentActivityUseCase getRecentActivityUseCase;
    private final GetSalesByCategoryUseCase getSalesByCategoryUseCase;

    @GetMapping("/sales-summary")
    @PreAuthorize("hasPermission(#orgId, 'Organization', 'VIEW_REPORTS')")
    @Operation(summary = "Get sales summary", description = "Returns total revenue, order count, and average ticket for a specific period")
    public ResponseEntity<SalesSummary> getSalesSummary(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {

        LocalDateTime start = (startDate != null) ? startDate.toLocalDateTime() : null;
        LocalDateTime end = (endDate != null) ? endDate.toLocalDateTime() : null;

        SalesSummary summary = getSalesSummaryUseCase.execute(orgId, start, end);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/top-products")
    @PreAuthorize("hasPermission(#orgId, 'Organization', 'VIEW_REPORTS')")
    @Operation(summary = "Get top selling products", description = "Returns the most popular products in a given date range")
    public ResponseEntity<List<TopSellingProduct>> getTopProducts(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @RequestParam(defaultValue = "10") int limit) {

        LocalDateTime start = (startDate != null) ? startDate.toLocalDateTime() : null;
        LocalDateTime end = (endDate != null) ? endDate.toLocalDateTime() : null;

        List<TopSellingProduct> products = getTopProductsUseCase.execute(orgId, start, end, limit);
        return ResponseEntity.ok(products);
    }
    @GetMapping("/recent-activity")
    @PreAuthorize("hasPermission(#orgId, 'Organization', 'VIEW_ORDERS')")
    @Operation(summary = "Get recent activity", description = "Returns the latest events in the restaurant (new orders, payments, etc.)")
    public ResponseEntity<List<RecentActivity>> getRecentActivity(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @RequestParam(defaultValue = "10") int limit) {

        List<RecentActivity> activities = getRecentActivityUseCase.execute(orgId, limit);
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/sales-by-category")
    @PreAuthorize("hasPermission(#orgId, 'Organization', 'VIEW_REPORTS')")
    @Operation(summary = "Get sales by category", description = "Returns revenue distribution across product categories")
    public ResponseEntity<List<CategorySales>> getSalesByCategory(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {

        LocalDateTime start = (startDate != null) ? startDate.toLocalDateTime() : null;
        LocalDateTime end = (endDate != null) ? endDate.toLocalDateTime() : null;

        List<CategorySales> sales = getSalesByCategoryUseCase.execute(orgId, start, end);
        return ResponseEntity.ok(sales);
    }
}
