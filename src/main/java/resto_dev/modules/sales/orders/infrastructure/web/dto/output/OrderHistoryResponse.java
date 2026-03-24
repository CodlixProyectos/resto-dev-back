package resto_dev.modules.sales.orders.infrastructure.web.dto.output;

public record OrderHistoryResponse(
        resto_dev.shared.responses.PaginatedResponse<OrderSummaryResponse> orders,
        java.math.BigDecimal totalRevenue
) {
}
