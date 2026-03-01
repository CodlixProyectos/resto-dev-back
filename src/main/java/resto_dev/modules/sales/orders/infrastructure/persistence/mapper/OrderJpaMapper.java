package resto_dev.modules.sales.orders.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import resto_dev.modules.sales.orders.domain.model.Order;
import resto_dev.modules.sales.orders.domain.model.OrderItem;
import resto_dev.modules.sales.orders.infrastructure.persistence.entity.OrderJpaEntity;
import resto_dev.modules.sales.orders.infrastructure.persistence.entity.OrderItemJpaEntity;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderJpaMapper {

    public Order toDomain(OrderJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        Order order = Order.builder()
                .id(entity.getId())
                .tableId(entity.getTableId())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .subtotal(entity.getSubtotal())
                .tax(entity.getTax())
                .total(entity.getTotal())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();

        if (entity.getItems() != null) {
            List<OrderItem> domainItems = new ArrayList<>();
            for (OrderItemJpaEntity itemEntity : entity.getItems()) {
                domainItems.add(toDomainItem(itemEntity, order.getId()));
            }
            order.setItems(domainItems);
        }

        return order;
    }

    private OrderItem toDomainItem(OrderItemJpaEntity itemEntity, java.util.UUID orderId) {
        if (itemEntity == null)
            return null;
        return OrderItem.builder()
                .id(itemEntity.getId())
                .orderId(orderId)
                .productId(itemEntity.getProductId())
                .productName(itemEntity.getProductName())
                .quantity(itemEntity.getQuantity())
                .unitPrice(itemEntity.getUnitPrice())
                .subtotal(itemEntity.getSubtotal())
                .notes(itemEntity.getNotes())
                .status(itemEntity.getStatus())
                .createdAt(itemEntity.getCreatedAt())
                .updatedAt(itemEntity.getUpdatedAt())
                .build();
    }

    public OrderJpaEntity toEntity(Order order) {
        if (order == null) {
            return null;
        }

        OrderJpaEntity entity = OrderJpaEntity.builder()
                .tableId(order.getTableId())
                .status(order.getStatus())
                .notes(order.getNotes())
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .total(order.getTotal())
                .build();

        if (order.getId() != null) {
            entity.setId(order.getId());
        }

        if (order.getItems() != null) {
            List<OrderItemJpaEntity> itemEntities = new ArrayList<>();
            for (OrderItem item : order.getItems()) {
                itemEntities.add(toEntityItem(item, entity));
            }
            // addItems takes care of setting the order reference correctly for cascade
            entity.addItems(itemEntities);
        }

        return entity;
    }

    private OrderItemJpaEntity toEntityItem(OrderItem item, OrderJpaEntity orderEntity) {
        if (item == null)
            return null;
        OrderItemJpaEntity entity = OrderItemJpaEntity.builder()
                .order(orderEntity)
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .notes(item.getNotes())
                .status(item.getStatus())
                .build();

        if (item.getId() != null) {
            entity.setId(item.getId());
        }
        return entity;
    }
}
