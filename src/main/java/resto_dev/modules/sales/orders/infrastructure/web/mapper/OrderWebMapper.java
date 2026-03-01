package resto_dev.modules.sales.orders.infrastructure.web.mapper;

import org.springframework.stereotype.Component;
import resto_dev.modules.sales.orders.application.command.CreateOrderCommand;
import resto_dev.modules.sales.orders.application.command.CreateOrderItemCommand;
import resto_dev.modules.sales.orders.domain.model.Order;
import resto_dev.modules.sales.orders.domain.model.OrderItem;
import resto_dev.modules.sales.orders.infrastructure.web.dto.input.CreateOrderRequest;
import resto_dev.modules.sales.orders.infrastructure.web.dto.output.OrderItemResponse;
import resto_dev.modules.sales.orders.infrastructure.web.dto.output.OrderResponse;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class OrderWebMapper {

    public CreateOrderCommand toCommand(CreateOrderRequest request) {
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
                .notes(request.notes())
                .items(items)
                .build();
    }

    public OrderResponse toResponse(Order order) {
        if (order == null)
            return null;

        var items = order.getItems() != null ? order.getItems().stream()
                .map(this::toResponseItem)
                .collect(Collectors.toList()) : Collections.<OrderItemResponse>emptyList();

        return new OrderResponse(
                order.getId(),
                order.getTableId(),
                order.getStatus(),
                order.getNotes(),
                order.getSubtotal(),
                order.getTax(),
                order.getTotal(),
                items,
                order.getCreatedAt());
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
}
