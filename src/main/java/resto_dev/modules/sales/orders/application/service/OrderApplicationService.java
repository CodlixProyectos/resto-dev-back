package resto_dev.modules.sales.orders.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import resto_dev.modules.layout.tables.application.port.output.TableRepositoryPort;
import resto_dev.modules.menu.products.application.port.output.ProductRepositoryPort;
import resto_dev.modules.menu.products.domain.model.Product;
import resto_dev.modules.sales.orders.application.command.CreateOrderCommand;
import resto_dev.modules.sales.orders.application.port.input.CreateOrderUseCase;
import resto_dev.modules.sales.orders.application.port.input.GetActiveKitchenOrdersUseCase;
import resto_dev.modules.sales.orders.application.port.input.UpdateOrderItemStatusUseCase;
import resto_dev.modules.sales.orders.application.port.output.KitchenEventPublisherPort;
import resto_dev.modules.sales.orders.application.port.output.OrderRepositoryPort;
import resto_dev.modules.sales.orders.application.query.SearchOrdersQuery;
import resto_dev.shared.tenancy.TenantContext;
import resto_dev.modules.sales.orders.domain.model.Order;
import resto_dev.modules.sales.orders.domain.model.OrderItem;
import resto_dev.modules.sales.orders.domain.model.OrderItemStatus;
import resto_dev.modules.sales.orders.domain.model.OrderStatus;
import resto_dev.shared.common.pagination.PageModel;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderApplicationService implements
        CreateOrderUseCase,
        GetActiveKitchenOrdersUseCase,
        UpdateOrderItemStatusUseCase {

    private final OrderRepositoryPort orderRepository;
    private final TableRepositoryPort tableRepository;
    private final ProductRepositoryPort productRepository;
    private final KitchenEventPublisherPort kitchenEventPublisher;

    @Override
    public Order execute(CreateOrderCommand command) {
        if (tableRepository.findById(command.tableId()).isEmpty()) {
            throw new IllegalArgumentException("La mesa seleccionada no existe o fue eliminada.");
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

        Order newOrder = Order.builder()
                .tableId(command.tableId())
                .status(OrderStatus.PENDING_KITCHEN)
                .notes(command.notes())
                .items(newItems)
                .build();

        newOrder.calculateTotals();

        Order savedOrder = orderRepository.save(newOrder);
        kitchenEventPublisher.publishOrderEvent(TenantContext.getCurrentOrganizationId(), savedOrder, "ORDER_CREATED");

        return savedOrder;
    }

    @Override
    public PageModel<Order> execute(SearchOrdersQuery query) {
        return orderRepository.searchOrders(query);
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
        if (newStatus == OrderItemStatus.PREPARING && order.getStatus() == OrderStatus.PENDING_KITCHEN) {
            order.setStatus(OrderStatus.PREPARING);
        }

        // If all dishes are ready/served/cancelled, the order jumps to READY_TO_SERVE
        // allowing waiter pickup.
        boolean allFinished = order.getItems().stream()
                .allMatch(i -> i.getStatus() == OrderItemStatus.READY ||
                        i.getStatus() == OrderItemStatus.SERVED ||
                        i.getStatus() == OrderItemStatus.CANCELLED);

        if (allFinished && order.getStatus() == OrderStatus.PREPARING) {
            order.setStatus(OrderStatus.READY_TO_SERVE);
        }

        Order savedOrder = orderRepository.save(order);
        kitchenEventPublisher.publishOrderEvent(TenantContext.getCurrentOrganizationId(), savedOrder, "ORDER_UPDATED");

        return savedOrder;
    }
}
