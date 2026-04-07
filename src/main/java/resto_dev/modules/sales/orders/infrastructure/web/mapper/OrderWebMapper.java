package resto_dev.modules.sales.orders.infrastructure.web.mapper;

import org.springframework.stereotype.Component;
import resto_dev.modules.sales.orders.application.command.CreateOrderCommand;
import resto_dev.modules.sales.orders.application.command.CreateOrderItemCommand;
import resto_dev.modules.sales.orders.domain.model.Order;
import resto_dev.modules.sales.orders.domain.model.OrderItem;
import resto_dev.modules.sales.orders.infrastructure.web.dto.input.CreateOrderRequest;
import resto_dev.modules.sales.orders.infrastructure.web.dto.output.OrderItemResponse;
import resto_dev.modules.sales.orders.infrastructure.web.dto.output.OrderResponse;
import resto_dev.modules.sales.orders.infrastructure.web.dto.output.OrderSummaryResponse;
import resto_dev.modules.sales.orders.infrastructure.web.dto.output.KitchenOrderItemResponse;
import resto_dev.modules.sales.orders.infrastructure.web.dto.output.KitchenOrderResponse;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class OrderWebMapper {

    private final resto_dev.modules.adminsaas.users.application.port.output.UserRepositoryPort userRepository;

    public OrderWebMapper(resto_dev.modules.adminsaas.users.application.port.output.UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    public CreateOrderCommand toCommand(CreateOrderRequest request, java.util.UUID waiterId) {
        if (request == null)
            return null;

        var items = request.items() != null ? request.items().stream()
                .map(i -> CreateOrderItemCommand.builder()
                        .productId(i.productId())
                        .quantity(i.quantity())
                        .notes(i.notes())
                        .build())
                .collect(Collectors.toList()) : Collections.<CreateOrderItemCommand>emptyList();

        return CreateOrderCommand.builder()
                .tableId(request.tableId())
                .type(request.type())
                .customerName(request.customerName())
                .notes(request.notes())
                .waiterId(waiterId)
                .items(items)
                .build();
    }

    public OrderResponse toResponse(Order order) {
        if (order == null)
            return null;

        var items = order.getItems() != null ? order.getItems().stream()
                .map(this::toResponseItem)
                .collect(Collectors.toList()) : Collections.<OrderItemResponse>emptyList();

        long minutesElapsed = java.time.Duration.between(order.getCreatedAt(), java.time.LocalDateTime.now()).toMinutes();
        boolean overdue = minutesElapsed >= 15;

        String waiterName = "Staff";
        if (order.getWaiterId() != null) {
            waiterName = userRepository.findById(order.getWaiterId())
                    .map(u -> u.getFullName())
                    .orElse("Staff");
        }

        return new OrderResponse(
                order.getId(),
                order.getTableId(),
                order.getTableNumber(),
                order.getType(),
                order.getCustomerName(),
                order.getStatus(),
                order.getNotes(),
                order.getSubtotal(),
                order.getTax(),
                order.getTotal(),
                items,
                order.getWaiterId(),
                waiterName,
                order.getCreatedAt(),
                minutesElapsed,
                overdue);
    }

    public OrderSummaryResponse toSummaryResponse(Order order) {
        if (order == null)
            return null;

        long minutesElapsed = java.time.Duration.between(order.getCreatedAt(), java.time.LocalDateTime.now()).toMinutes();
        boolean overdue = minutesElapsed >= 15;

        String waiterName = "Staff";
        if (order.getWaiterId() != null) {
            waiterName = userRepository.findById(order.getWaiterId())
                    .map(u -> u.getFullName())
                    .orElse("Staff");
        }

        var items = order.getItems() != null ? order.getItems().stream()
                .map(this::toResponseItem)
                .collect(Collectors.toList()) : java.util.Collections.<OrderItemResponse>emptyList();

        return new OrderSummaryResponse(
                order.getId(),
                order.getTableId(),
                order.getTableNumber(),
                order.getType(),
                order.getCustomerName(),
                order.getStatus(),
                order.getNotes(),
                order.getSubtotal(),
                order.getTax(),
                order.getTotal(),
                order.getWaiterId(),
                waiterName,
                items,
                order.getCreatedAt(),
                minutesElapsed,
                overdue);
    }

    private OrderItemResponse toResponseItem(OrderItem item) {
        if (item == null)
            return null;
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal(),
                item.getNotes(),
                item.getStatus());
    }

    public KitchenOrderResponse toKitchenResponse(Order order) {
        if (order == null)
            return null;

        var items = order.getItems() != null ? order.getItems().stream()
                .map(this::toKitchenResponseItem)
                .collect(Collectors.toList()) : Collections.<KitchenOrderItemResponse>emptyList();

        long minutesElapsed = java.time.Duration.between(order.getCreatedAt(), java.time.LocalDateTime.now()).toMinutes();
        boolean overdue = minutesElapsed >= 15;

        String waiterName = "Staff";
        if (order.getWaiterId() != null) {
            waiterName = userRepository.findById(order.getWaiterId())
                    .map(u -> u.getFullName())
                    .orElse("Staff");
        }

        return new KitchenOrderResponse(
                order.getId(),
                order.getTableId(),
                order.getTableNumber(),
                order.getCustomerName(),
                order.getStatus(),
                order.getNotes(),
                items,
                order.getWaiterId(),
                waiterName,
                order.getCreatedAt(),
                minutesElapsed,
                overdue);
    }

    private KitchenOrderItemResponse toKitchenResponseItem(OrderItem item) {
        if (item == null)
            return null;
        return new KitchenOrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getNotes(),
                item.getStatus());
    }
}
