package resto_dev.modules.sales.orders.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.sales.orders.application.port.input.CreateOrderUseCase;
import resto_dev.modules.sales.orders.application.port.input.GetOrderByIdUseCase;
import resto_dev.modules.sales.orders.application.port.input.GetActiveKitchenOrdersUseCase;
import resto_dev.modules.sales.orders.application.port.input.UpdateOrderItemStatusUseCase;
import resto_dev.modules.sales.orders.application.port.input.UpdateOrderStatusUseCase;
import resto_dev.modules.sales.orders.application.query.SearchOrdersQuery;
import resto_dev.modules.sales.orders.domain.model.Order;
import resto_dev.modules.sales.orders.domain.model.OrderStatus;
import resto_dev.modules.sales.orders.infrastructure.web.dto.input.CreateOrderRequest;
import resto_dev.modules.sales.orders.infrastructure.web.dto.input.UpdateOrderItemStatusRequest;
import resto_dev.modules.sales.orders.infrastructure.web.dto.input.UpdateOrderStatusRequest;
import resto_dev.modules.sales.orders.infrastructure.web.dto.output.OrderResponse;
import resto_dev.modules.sales.orders.infrastructure.web.dto.output.OrderSummaryResponse;
import resto_dev.modules.sales.orders.infrastructure.web.dto.output.OrderHistoryResponse;
import resto_dev.modules.sales.orders.infrastructure.web.dto.output.KitchenOrderResponse;
import resto_dev.modules.sales.orders.infrastructure.web.mapper.OrderWebMapper;
import resto_dev.shared.common.pagination.PageModel;
import resto_dev.shared.responses.ApiResponse;
import resto_dev.shared.responses.PaginatedResponse;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Órdenes y KDS (Kitchen)", description = "Endpoints para la gestión de comandas por meseros y vistas de cocina interactiva")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetActiveKitchenOrdersUseCase getActiveKitchenOrdersUseCase;
    private final UpdateOrderItemStatusUseCase updateOrderItemStatusUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final resto_dev.modules.sales.orders.application.port.input.GetMyOrderHistoryUseCase getMyOrderHistoryUseCase;
    private final OrderWebMapper webMapper;
    private final resto_dev.modules.sales.orders.infrastructure.web.sse.SseKitchenEventPublisher sseKitchenEventPublisher;
    private final resto_dev.modules.sales.orders.infrastructure.web.sse.SseWaiterEventPublisher sseWaiterEventPublisher;
    private final resto_dev.modules.sales.orders.infrastructure.web.sse.SseAdminEventPublisher sseAdminEventPublisher;
    private final resto_dev.shared.security.sse.SseTicketService sseTicketService;
    private final GetOrderByIdUseCase getOrderByIdUseCase;

    public OrderController(
            CreateOrderUseCase createOrderUseCase,
            GetActiveKitchenOrdersUseCase getActiveKitchenOrdersUseCase,
            UpdateOrderItemStatusUseCase updateOrderItemStatusUseCase,
            UpdateOrderStatusUseCase updateOrderStatusUseCase,
            resto_dev.modules.sales.orders.application.port.input.GetMyOrderHistoryUseCase getMyOrderHistoryUseCase,
            OrderWebMapper webMapper,
            resto_dev.modules.sales.orders.infrastructure.web.sse.SseKitchenEventPublisher sseKitchenEventPublisher,
            resto_dev.modules.sales.orders.infrastructure.web.sse.SseWaiterEventPublisher sseWaiterEventPublisher,
            resto_dev.modules.sales.orders.infrastructure.web.sse.SseAdminEventPublisher sseAdminEventPublisher,
            resto_dev.shared.security.sse.SseTicketService sseTicketService,
            GetOrderByIdUseCase getOrderByIdUseCase) {
        this.createOrderUseCase = createOrderUseCase;
        this.getActiveKitchenOrdersUseCase = getActiveKitchenOrdersUseCase;
        this.updateOrderItemStatusUseCase = updateOrderItemStatusUseCase;
        this.updateOrderStatusUseCase = updateOrderStatusUseCase;
        this.getMyOrderHistoryUseCase = getMyOrderHistoryUseCase;
        this.webMapper = webMapper;
        this.sseKitchenEventPublisher = sseKitchenEventPublisher;
        this.sseWaiterEventPublisher = sseWaiterEventPublisher;
        this.sseAdminEventPublisher = sseAdminEventPublisher;
        this.sseTicketService = sseTicketService;
        this.getOrderByIdUseCase = getOrderByIdUseCase;
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Obtener detalle de orden", description = "Devuelve toda la información de una orden específica, incluyendo sus platos.")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable UUID orderId) {
        Order order = getOrderByIdUseCase.execute(orderId);
        return ResponseEntity.ok(ApiResponse.ok(webMapper.toResponse(order)));
    }

    @PostMapping("/sse-ticket")
    @Operation(summary = "Generar Ticket SSE", description = "Genera un ticket de un solo uso para autenticar una conexión SSE sin exponer el JWT en la URL.")
    public ResponseEntity<ApiResponse<UUID>> generateSseTicket(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal UUID userId) {
        UUID ticket = sseTicketService.generateTicket(userId, orgId);
        return ResponseEntity.ok(ApiResponse.ok(ticket, "Ticket generado exitosamente"));
    }

    @PostMapping("/create")
    @PreAuthorize("hasPermission(#orgId, 'Organization', 'CREATE_ORDER')")
    @Operation(summary = "Crear orden de mesa", description = "El mesero registra el pedido de una mesa. Estado inicial PENDING_KITCHEN.")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal UUID waiterId,
            @Valid @RequestBody CreateOrderRequest request) {
        Order createdOrder = createOrderUseCase.execute(webMapper.toCommand(request, waiterId));
        return new ResponseEntity<>(
                ApiResponse.created(webMapper.toResponse(createdOrder), "Orden creada exitosamente"),
                HttpStatus.CREATED);
    }

    @GetMapping(value = "/admin/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Admin Stream (Eventos SSE)", description = "Conexión en tiempo real para notificaciones globales de la organización (pedidos, stock, reservas).")
    public SseEmitter streamAdminNotifications(
            @RequestParam(value = "organizationId", required = false) UUID orgIdParam,
            @RequestHeader(value = "X-Organization-Id", required = false) UUID orgIdHeader) {
        
        UUID orgId = orgIdParam != null ? orgIdParam : (orgIdHeader != null ? orgIdHeader : resto_dev.shared.tenancy.TenantContext.getCurrentOrganizationId());
        
        if (orgId == null) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new RuntimeException("Organization ID required"));
            return emitter;
        }
        
        return sseAdminEventPublisher.subscribe(orgId);
    }

    @GetMapping(value = "/kitchen/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "KDS Stream (Eventos SSE)", description = "Conexión persistente donde la pantalla de cocina recibe actualizaciones en tiempo real.")
    public SseEmitter streamKitchenOrders(
            @RequestParam(value = "organizationId", required = false) UUID orgIdParam,
            @RequestHeader(value = "X-Organization-Id", required = false) UUID orgIdHeader) {
        
        UUID orgId = orgIdParam != null ? orgIdParam : (orgIdHeader != null ? orgIdHeader : resto_dev.shared.tenancy.TenantContext.getCurrentOrganizationId());
        
        if (orgId == null) {
            log.warn("Attempted to subscribe to kitchen stream without organization ID");
            // Returning emitter that will immediately close or handle error
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new RuntimeException("Organization ID required"));
            return emitter;
        }
        
        return sseKitchenEventPublisher.subscribe(orgId);
    }

    @GetMapping(value = "/waiter/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Waiter Stream (Eventos SSE)", description = "Conexión en tiempo real para el mesero autenticado.")
    public SseEmitter streamWaiterOrders(@org.springframework.security.core.annotation.AuthenticationPrincipal UUID waiterId) {
        return sseWaiterEventPublisher.subscribe(waiterId);
    }

    @GetMapping("/kitchen/active")
    @PreAuthorize("hasPermission(#orgId, 'Organization', 'VIEW_KITCHEN')")
    @Operation(summary = "Vista de Cocina (KDS)", description = "Devuelve las órdenes que están listas para que la cocina las prepare (ordenadas de las más antiguas a las más recientes).")
    public ResponseEntity<ApiResponse<PaginatedResponse<KitchenOrderResponse>>> getActiveKitchenOrders(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        SearchOrdersQuery query = SearchOrdersQuery.builder()
                .statuses(List.of(OrderStatus.PENDING_KITCHEN, OrderStatus.PREPARING, OrderStatus.READY_TO_SERVE))
                .page(page)
                .size(size)
                .build();

        PageModel<Order> pageModel = getActiveKitchenOrdersUseCase.execute(query);

        List<KitchenOrderResponse> responses = pageModel.content().stream()
                .map(webMapper::toKitchenResponse)
                .collect(Collectors.toList());

        PaginatedResponse<KitchenOrderResponse> pageResponse = PaginatedResponse.<KitchenOrderResponse>builder()
                .data(responses)
                .page(pageModel.page())
                .size(pageModel.size())
                .totalElements(pageModel.totalElements())
                .totalPages(pageModel.totalPages())
                .hasNext(pageModel.page() < pageModel.totalPages() - 1)
                .hasPrevious(pageModel.page() > 0)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(pageResponse));
    }

    @GetMapping("/kitchen/history")
    @PreAuthorize("hasPermission(#orgId, 'Organization', 'VIEW_KITCHEN')")
    @Operation(summary = "Historial de Cocina (Entregados Hoy)", description = "Devuelve las órdenes que fueron entregadas el día de hoy.")
    public ResponseEntity<ApiResponse<PaginatedResponse<KitchenOrderResponse>>> getKitchenHistory(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        java.time.LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();

        SearchOrdersQuery query = SearchOrdersQuery.builder()
                .statuses(List.of(OrderStatus.DELIVERED))
                .startDate(startOfDay)
                .page(page)
                .size(size)
                .build();

        PageModel<Order> pageModel = getActiveKitchenOrdersUseCase.execute(query);

        List<KitchenOrderResponse> responses = pageModel.content().stream()
                .map(webMapper::toKitchenResponse)
                .collect(Collectors.toList());

        PaginatedResponse<KitchenOrderResponse> pageResponse = PaginatedResponse.<KitchenOrderResponse>builder()
                .data(responses)
                .page(pageModel.page())
                .size(pageModel.size())
                .totalElements(pageModel.totalElements())
                .totalPages(pageModel.totalPages())
                .hasNext(pageModel.page() < pageModel.totalPages() - 1)
                .hasPrevious(pageModel.page() > 0)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(pageResponse));
    }

    @GetMapping("/kitchen/stats")
    @PreAuthorize("hasPermission(#orgId, 'Organization', 'VIEW_KITCHEN')")
    @Operation(summary = "KDS: Estadísticas del Dashboard", description = "Devuelve los contadores de órdenes por estado (Pendientes, Preparando, Listos y Entregados hoy).")
    public ResponseEntity<ApiResponse<resto_dev.modules.sales.orders.infrastructure.web.dto.output.KitchenDashboardStatsResponse>> getKitchenDashboardStats(
            @RequestHeader("X-Organization-Id") UUID orgId) {
        
        var stats = ((resto_dev.modules.sales.orders.application.port.input.GetKitchenDashboardStatsUseCase) getActiveKitchenOrdersUseCase).execute();
        
        return ResponseEntity.ok(ApiResponse.ok(new resto_dev.modules.sales.orders.infrastructure.web.dto.output.KitchenDashboardStatsResponse(
                stats.pendingCount(),
                stats.preparingCount(),
                stats.readyCount(),
                stats.deliveredTodayCount()
        )));
    }

    @GetMapping("/my-history")
    @Operation(summary = "Mi Historial de Pedidos", description = "Devuelve los pedidos realizados por el mesero autenticado.")
    public ResponseEntity<ApiResponse<PaginatedResponse<OrderSummaryResponse>>> getMyOrderHistory(
            @org.springframework.security.core.annotation.AuthenticationPrincipal UUID waiterId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.OffsetDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.OffsetDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        SearchOrdersQuery query = SearchOrdersQuery.builder()
                .waiterId(waiterId)
                .startDate(startDate != null ? startDate.toLocalDateTime() : null)
                .endDate(endDate != null ? endDate.toLocalDateTime() : null)
                .page(page)
                .size(size)
                .build();

        PageModel<Order> pageModel = getMyOrderHistoryUseCase.execute(query);

        List<OrderSummaryResponse> responses = pageModel.content().stream()
                .map(webMapper::toSummaryResponse)
                .collect(Collectors.toList());

        PaginatedResponse<OrderSummaryResponse> pageResponse = PaginatedResponse.<OrderSummaryResponse>builder()
                .data(responses)
                .page(pageModel.page())
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
    public ResponseEntity<ApiResponse<KitchenOrderResponse>> updateOrderItemStatus(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @PathVariable UUID orderId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateOrderItemStatusRequest request) {

        Order updatedOrder = updateOrderItemStatusUseCase.execute(orderId, itemId, request.newStatus());
        return ResponseEntity.ok(ApiResponse.ok(webMapper.toKitchenResponse(updatedOrder)));
    }

    @PutMapping("/update/{orderId}/status")
    @PreAuthorize("hasPermission(#orgId, 'Organization', 'UPDATE_KITCHEN')")
    @Operation(summary = "Cocina: Avanzar estado de la orden (Atómico)", description = "El cocinero marca toda la orden como PREPARING o READY_TO_SERVE en un solo paso.")
    public ResponseEntity<ApiResponse<KitchenOrderResponse>> updateOrderStatus(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        Order updatedOrder = updateOrderStatusUseCase.execute(orderId, request.newStatus());
        return ResponseEntity.ok(ApiResponse.ok(webMapper.toKitchenResponse(updatedOrder)));
    }

    @GetMapping("/search")
    @Operation(summary = "Búsqueda General de Pedidos", description = "Permite a los administradores buscar órdenes en toda la organización con múltiples filtros.")
    public ResponseEntity<ApiResponse<OrderHistoryResponse>> searchOrders(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @RequestParam(required = false) UUID waiterId,
            @RequestParam(required = false) UUID tableId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.OffsetDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.OffsetDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        SearchOrdersQuery query = SearchOrdersQuery.builder()
                .waiterId(waiterId)
                .tableId(tableId)
                .statuses(status != null ? List.of(status) : null)
                .searchTerm(searchTerm)
                .startDate(startDate != null ? startDate.toLocalDateTime() : null)
                .endDate(endDate != null ? endDate.toLocalDateTime() : null)
                .page(page)
                .size(size)
                .build();

        PageModel<Order> pageModel = getActiveKitchenOrdersUseCase.execute(query);
        java.math.BigDecimal totalRevenue = getActiveKitchenOrdersUseCase.calculateRevenue(query);

        List<OrderSummaryResponse> responses = pageModel.content().stream()
                .map(webMapper::toSummaryResponse)
                .collect(Collectors.toList());

        PaginatedResponse<OrderSummaryResponse> paginatedData = PaginatedResponse.<OrderSummaryResponse>builder()
                .data(responses)
                .page(pageModel.page())
                .size(pageModel.size())
                .totalElements(pageModel.totalElements())
                .totalPages(pageModel.totalPages())
                .hasNext(pageModel.page() < pageModel.totalPages() - 1)
                .hasPrevious(pageModel.page() > 0)
                .build();

        OrderHistoryResponse historyResponse = new OrderHistoryResponse(paginatedData, totalRevenue);

        return ResponseEntity.ok(ApiResponse.ok(historyResponse));
    }
}
