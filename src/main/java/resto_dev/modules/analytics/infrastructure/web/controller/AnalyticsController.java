package resto_dev.modules.analytics.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.analytics.application.port.input.GetSalesSummaryUseCase;
import resto_dev.modules.analytics.application.port.input.GetTopProductsUseCase;
import resto_dev.modules.analytics.domain.model.SalesSummary;
import resto_dev.modules.analytics.domain.model.TopSellingProduct;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Endpoints for restaurant dashboard statistics and reports")
public class AnalyticsController {

    private final GetSalesSummaryUseCase getSalesSummaryUseCase;
    private final GetTopProductsUseCase getTopProductsUseCase;

    @GetMapping("/sales-summary")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    @Operation(summary = "Get sales summary", description = "Returns total revenue, order count, and average ticket for a specific period")
    public ResponseEntity<SalesSummary> getSalesSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        SalesSummary summary = getSalesSummaryUseCase.execute(startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/top-products")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    @Operation(summary = "Get top selling products", description = "Returns the most popular products in a given date range")
    public ResponseEntity<List<TopSellingProduct>> getTopProducts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "10") int limit) {

        List<TopSellingProduct> products = getTopProductsUseCase.execute(startDate, endDate, limit);
        return ResponseEntity.ok(products);
    }
}
