package resto_dev.modules.sales.orders.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.layout.tables.application.port.output.TableRepositoryPort;
import resto_dev.modules.layout.tables.domain.model.TableStatus;
import resto_dev.modules.menu.products.application.port.output.ProductRepositoryPort;
import resto_dev.modules.menu.products.domain.model.Product;
import resto_dev.modules.sales.orders.application.command.CreateOrderCommand;
import resto_dev.modules.sales.orders.application.port.input.CreateOrderUseCase;
import resto_dev.modules.sales.orders.application.port.input.GetActiveKitchenOrdersUseCase;
import resto_dev.modules.sales.orders.application.port.input.GetMyOrderHistoryUseCase;
import resto_dev.modules.sales.orders.application.port.input.UpdateOrderItemStatusUseCase;
import resto_dev.modules.sales.orders.application.port.input.UpdateOrderStatusUseCase;
import resto_dev.modules.sales.orders.application.port.output.KitchenEventPublisherPort;
import resto_dev.modules.sales.orders.application.port.output.OrderRepositoryPort;
import resto_dev.modules.sales.orders.application.query.SearchOrdersQuery;
import resto_dev.shared.tenancy.TenantContext;
import resto_dev.modules.sales.orders.domain.model.Order;
import resto_dev.modules.sales.orders.domain.model.OrderItem;
import resto_dev.modules.sales.orders.domain.model.OrderItemStatus;
import resto_dev.modules.sales.orders.domain.model.OrderStatus;
import resto_dev.modules.sales.orders.domain.model.OrderType;
import resto_dev.shared.common.pagination.PageModel;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderApplicationService implements
        CreateOrderUseCase,
        GetActiveKitchenOrdersUseCase,
        GetMyOrderHistoryUseCase,
        UpdateOrderItemStatusUseCase,
        UpdateOrderStatusUseCase,
        resto_dev.modules.sales.orders.application.port.input.GetOrderByIdUseCase,
        resto_dev.modules.sales.orders.application.port.input.GetKitchenDashboardStatsUseCase {

    private final OrderRepositoryPort orderRepository;
    private final TableRepositoryPort tableRepository;
    private final ProductRepositoryPort productRepository;
    private final KitchenEventPublisherPort kitchenEventPublisher;
    private final resto_dev.modules.sales.orders.application.port.output.WaiterEventPublisherPort waiterEventPublisher;

    public OrderApplicationService(
            OrderRepositoryPort orderRepository,
            TableRepositoryPort tableRepository,
            ProductRepositoryPort productRepository,
            KitchenEventPublisherPort kitchenEventPublisher,
            resto_dev.modules.sales.orders.application.port.output.WaiterEventPublisherPort waiterEventPublisher) {
        this.orderRepository = orderRepository;
        this.tableRepository = tableRepository;
        this.productRepository = productRepository;
        this.kitchenEventPublisher = kitchenEventPublisher;
        this.waiterEventPublisher = waiterEventPublisher;
    }

    @Override
    public Order execute(CreateOrderCommand command) {
        if (command.type() == OrderType.DINE_IN && command.tableId() != null) {
            if (tableRepository.findById(command.tableId()).isEmpty()) {
                throw new IllegalArgumentException("La mesa seleccionada no existe o fue eliminada.");
            }
        }

        if (command.items() == null || command.items().isEmpty()) {
            throw new IllegalArgumentException("La orden debe tener al menos un producto.");
        }

        List<OrderItem> newItems = command.items().stream().map(cmdItem -> {
            Product product = productRepository.findById(cmdItem.productId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "El producto con ID " + cmdItem.productId() + " no existe en el menú."));

            if (!product.isActive()) {
                throw new IllegalArgumentException(
                        "El producto '" + product.getName() + "' no está disponible actualmente.");
            }

            return OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .unitPrice(product.getPrice())
                    .quantity(cmdItem.quantity())
                    .notes(cmdItem.notes())
                    .status(OrderItemStatus.PENDING)
                    .build();
        }).collect(Collectors.toList());

        String tableNumber = null;
        if (command.type() == OrderType.DINE_IN && command.tableId() != null) {
            tableNumber = tableRepository.findById(command.tableId())
                    .map(resto_dev.modules.layout.tables.domain.model.Table::getTableNumber)
                    .orElse(null);
        }

        Order newOrder = Order.builder()
                .tableId(command.type() == OrderType.DINE_IN ? command.tableId() : null)
                .tableNumber(tableNumber)
                .type(command.type())
                .customerName(command.customerName())
                .status(OrderStatus.PENDING_KITCHEN)
                .notes(command.notes())
                .items(newItems)
                .waiterId(command.waiterId())
                .build();

        newOrder.calculateTotals();

        Order savedOrder = orderRepository.save(newOrder);

        // Automate Table Status: OCCUPIED
        if (savedOrder.getType() == OrderType.DINE_IN && savedOrder.getTableId() != null) {
            tableRepository.findById(savedOrder.getTableId()).ifPresent(table -> {
                table.setStatus(TableStatus.OCCUPIED);
                tableRepository.save(table);
            });
        }

        kitchenEventPublisher.publishOrderEvent(TenantContext.getCurrentOrganizationId(), savedOrder, "ORDER_CREATED");

        return savedOrder;
    }

    @Override
    public PageModel<Order> execute(SearchOrdersQuery query) {
        return orderRepository.searchOrders(query);
    }

    @Override
    public java.math.BigDecimal calculateRevenue(SearchOrdersQuery query) {
        return orderRepository.sumTotalByQuery(query);
    }

    @Override
    public Order execute(UUID orderId, UUID itemId, OrderItemStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("La orden solicitada no fue encontrada."));

        OrderItem targetItem = order.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El ítem indicado no existe en esta orden."));

        targetItem.setStatus(newStatus);

        // Logical Transitions - If kitchen drops the first item to PREPARING, whole
        // order is PREPARING.
        if ((newStatus == OrderItemStatus.PREPARING || newStatus == OrderItemStatus.READY) 
            && order.getStatus() == OrderStatus.PENDING_KITCHEN) {
            order.setStatus(OrderStatus.PREPARING);
        }

        // If all dishes are ready/served/cancelled, the order jumps to READY_TO_SERVE
        // allowing waiter pickup.
        boolean allFinished = order.getItems().stream()
                .allMatch(i -> i.getStatus() == OrderItemStatus.READY ||
                        i.getStatus() == OrderItemStatus.SERVED ||
                        i.getStatus() == OrderItemStatus.CANCELLED);

        if (allFinished && (order.getStatus() == OrderStatus.PREPARING || order.getStatus() == OrderStatus.PENDING_KITCHEN)) {
            order.setStatus(OrderStatus.READY_TO_SERVE);
        }

        Order savedOrder = orderRepository.save(order);
        kitchenEventPublisher.publishOrderEvent(TenantContext.getCurrentOrganizationId(), savedOrder, "ORDER_UPDATED");

        if (savedOrder.getStatus() == OrderStatus.READY_TO_SERVE && savedOrder.getWaiterId() != null) {
            waiterEventPublisher.notifyWaiter(TenantContext.getCurrentOrganizationId(), savedOrder.getWaiterId(), savedOrder, "ORDER_READY");
        }
        return savedOrder;
    }

    @Override
    public Order execute(UUID orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("La orden solicitada no fue encontrada."));

        order.setStatus(newStatus);

        // Sync items status based on order status
        OrderItemStatus itemStatus = switch (newStatus) {
            case PREPARING -> OrderItemStatus.PREPARING;
            case READY_TO_SERVE -> OrderItemStatus.READY;
            case DELIVERED -> OrderItemStatus.SERVED;
            case PAID -> OrderItemStatus.SERVED;
            case CANCELLED -> OrderItemStatus.CANCELLED;
            default -> null;
        };

        if (itemStatus != null) {
            order.getItems().forEach(item -> {
                // Only advance status, don't regress unless it's a cancellation
                if (newStatus == OrderStatus.CANCELLED || 
                   (newStatus == OrderStatus.READY_TO_SERVE && item.getStatus() != OrderItemStatus.READY) ||
                   (newStatus == OrderStatus.PREPARING && item.getStatus() == OrderItemStatus.PENDING)) {
                    item.setStatus(itemStatus);
                }
            });
        }

        Order savedOrder = orderRepository.save(order);

        // Automate Table Status: FREE on Cancellation or Payment
        if ((newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.PAID) && savedOrder.getTableId() != null) {
            // Check if there are other active orders for this table
            boolean hasOtherActiveOrders = orderRepository.searchOrders(SearchOrdersQuery.builder()
                            .tableId(savedOrder.getTableId())
                            .statuses(List.of(
                                    OrderStatus.PENDING_KITCHEN,
                                    OrderStatus.PREPARING,
                                    OrderStatus.READY_TO_SERVE,
                                    OrderStatus.DELIVERED))
                            .page(0)
                            .size(1)
                            .build())
                    .content().stream().anyMatch(o -> !o.getId().equals(savedOrder.getId()));

            if (!hasOtherActiveOrders) {
                tableRepository.findById(savedOrder.getTableId()).ifPresent(table -> {
                    table.setStatus(TableStatus.FREE);
                    tableRepository.save(table);
                });
            }
        }
        if (newStatus != OrderStatus.PAID) {
            kitchenEventPublisher.publishOrderEvent(TenantContext.getCurrentOrganizationId(), savedOrder, "ORDER_UPDATED");
        }

        if (savedOrder.getStatus() == OrderStatus.READY_TO_SERVE && savedOrder.getWaiterId() != null) {
            waiterEventPublisher.notifyWaiter(TenantContext.getCurrentOrganizationId(), savedOrder.getWaiterId(), savedOrder, "ORDER_READY");
        }

        return savedOrder;
    }

    @Override
    public Order execute(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La orden con ID " + id + " no existe."));
    }

    @Override
    @Transactional(readOnly = true)
    public resto_dev.modules.sales.orders.domain.model.KitchenDashboardStats execute() {
        return resto_dev.modules.sales.orders.domain.model.KitchenDashboardStats.builder()
                .pendingCount(orderRepository.countByStatus(OrderStatus.PENDING_KITCHEN))
                .preparingCount(orderRepository.countByStatus(OrderStatus.PREPARING))
                .readyCount(orderRepository.countByStatus(OrderStatus.READY_TO_SERVE))
                .deliveredTodayCount(orderRepository.countByStatusAndDate(OrderStatus.DELIVERED, java.time.LocalDate.now()))
                .build();
    }
}
