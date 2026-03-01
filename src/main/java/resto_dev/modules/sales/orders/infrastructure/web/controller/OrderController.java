package resto_dev.modules.sales.orders.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.sales.orders.application.port.input.CreateOrderUseCase;
import resto_dev.modules.sales.orders.application.port.input.GetActiveKitchenOrdersUseCase;
import resto_dev.modules.sales.orders.application.port.input.UpdateOrderItemStatusUseCase;
import resto_dev.modules.sales.orders.application.query.SearchOrdersQuery;
import resto_dev.modules.sales.orders.domain.model.Order;
import resto_dev.modules.sales.orders.domain.model.OrderStatus;
import resto_dev.modules.sales.orders.infrastructure.web.dto.input.CreateOrderRequest;
import resto_dev.modules.sales.orders.infrastructure.web.dto.input.UpdateOrderItemStatusRequest;
import resto_dev.modules.sales.orders.infrastructure.web.dto.output.OrderResponse;
import resto_dev.modules.sales.orders.infrastructure.web.mapper.OrderWebMapper;
import resto_dev.shared.common.pagination.PageModel;
import resto_dev.shared.responses.ApiResponse;
import resto_dev.shared.responses.PaginatedResponse;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Órdenes y KDS (Kitchen)", description = "Endpoints para la gestión de comandas por meseros y vistas de cocina interactiva")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetActiveKitchenOrdersUseCase getActiveKitchenOrdersUseCase;
    private final UpdateOrderItemStatusUseCase updateOrderItemStatusUseCase;
    private final OrderWebMapper webMapper;
    private final resto_dev.modules.sales.orders.infrastructure.web.sse.SseKitchenEventPublisher sseKitchenEventPublisher;

    @PostMapping("/create")
    @PreAuthorize("hasPermission(#orgId, 'Organization', 'CREATE_ORDER')")
    @Operation(summary = "Crear orden de mesa", description = "El mesero registra el pedido de una mesa. Estado inicial PENDING_KITCHEN.")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @Valid @RequestBody CreateOrderRequest request) {
        Order createdOrder = createOrderUseCase.execute(webMapper.toCommand(request));
        return new ResponseEntity<>(
                ApiResponse.created(webMapper.toResponse(createdOrder), "Orden creada exitosamente"),
                HttpStatus.CREATED);
    }

    @GetMapping(value = "/kitchen/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasPermission(#orgId, 'Organization', 'VIEW_KITCHEN')")
    @Operation(summary = "KDS Stream (Eventos SSE)", description = "Conexión persistente donde la pantalla de cocina recibe actualizaciones en tiempo real.")
    public SseEmitter streamKitchenOrders(@RequestHeader("X-Organization-Id") UUID orgId) {
        return sseKitchenEventPublisher.subscribe(orgId);
    }

    @GetMapping("/kitchen/active")
    @PreAuthorize("hasPermission(#orgId, 'Organization', 'VIEW_KITCHEN')")
    @Operation(summary = "Vista de Cocina (KDS)", description = "Devuelve las órdenes que están listas para que la cocina las prepare (ordenadas de las más antiguas a las más recientes).")
    public ResponseEntity<ApiResponse<PaginatedResponse<OrderResponse>>> getActiveKitchenOrders(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        SearchOrdersQuery query = SearchOrdersQuery.builder()
                .statuses(List.of(OrderStatus.PENDING_KITCHEN, OrderStatus.PREPARING))
                .page(page)
                .size(size)
                .build();

        PageModel<Order> pageModel = getActiveKitchenOrdersUseCase.execute(query);

        List<OrderResponse> responses = pageModel.content().stream()
                .map(webMapper::toResponse)
                .collect(Collectors.toList());

        PaginatedResponse<OrderResponse> pageResponse = PaginatedResponse.<OrderResponse>builder()
                .data(responses)
                .page(pageModel.page() + 1)
                .size(pageModel.size())
                .totalElements(pageModel.totalElements())
                .totalPages(pageModel.totalPages())
                .hasNext(pageModel.page() < pageModel.totalPages() - 1)
                .hasPrevious(pageModel.page() > 0)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(pageResponse));
    }

    @PutMapping("/update/{orderId}/items/{itemId}/status")
    @PreAuthorize("hasPermission(#orgId, 'Organization', 'UPDATE_KITCHEN')")
    @Operation(summary = "Cocina: Avanzar estado del plato", description = "El cocinero toca un plato y lo marca como PREPARING o READY. El status de la orden global se auto-calcula.")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderItemStatus(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @PathVariable UUID orderId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateOrderItemStatusRequest request) {

        Order updatedOrder = updateOrderItemStatusUseCase.execute(orderId, itemId, request.newStatus());
        return ResponseEntity.ok(ApiResponse.ok(webMapper.toResponse(updatedOrder)));
    }
}
