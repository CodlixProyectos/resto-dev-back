package resto_dev.modules.sales.orders.infrastructure.web.mapper;

import org.springframework.stereotype.Component;
import resto_dev.modules.sales.orders.domain.model.Order;
import resto_dev.modules.sales.orders.infrastructure.web.dto.output.AdminNotificationResponse;
import resto_dev.modules.inventory.infrastructure.persistence.entity.InventoryItemJpaEntity;
import resto_dev.modules.reservations.domain.model.Reservation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class AdminNotificationMapper {

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public AdminNotificationResponse fromOrder(Order order, String eventType) {
        boolean isCreated = "ORDER_CREATED".equals(eventType);
        String message = switch (order.getType()) {
            case TAKEOUT -> isCreated ? "Se ha recibido un nuevo pedido para llevar." : "El pedido para llevar ha sido completado.";
            case DELIVERY -> isCreated ? "Se ha recibido un nuevo pedido por delivery." : "El pedido de delivery ha sido completado.";
            default -> isCreated 
                ? "La Mesa " + order.getTableNumber() + " ha realizado un pedido." 
                : "La mesa " + order.getTableNumber() + " ha cerrado su cuenta exitosamente.";
        };

        String title = isCreated ? "Nuevo Pedido" : "Venta Completada";
        String type = isCreated ? "info" : "success";

        return AdminNotificationResponse.builder()
                .id(UUID.randomUUID().toString())
                .title(title)
                .message(message)
                .type(type)
                .status("new")
                .timestamp(LocalDateTime.now().format(formatter))
                .relatedId(order.getId().toString())
                .relatedType("ORDER")
                .actionUrl("/app/sales/history")
                .build();
    }

    public AdminNotificationResponse fromInventoryItem(InventoryItemJpaEntity item) {
        return AdminNotificationResponse.builder()
                .id(UUID.randomUUID().toString())
                .title("Stock Bajo")
                .message("El producto \"" + item.getName() + "\" está por debajo del nivel mínimo.")
                .type("warning")
                .status("new")
                .timestamp(LocalDateTime.now().format(formatter))
                .relatedId(item.getId().toString())
                .relatedType("INVENTORY")
                .actionUrl("/app/inventory")
                .build();
    }

    public AdminNotificationResponse fromReservation(Reservation res) {
        return AdminNotificationResponse.builder()
                .id(UUID.randomUUID().toString())
                .title("Nueva Reserva")
                .message(res.getCustomerName() + " ha reservado una mesa para " + res.getNumGuests() + " personas para el " + res.getReservationDate() + ".")
                .type("message")
                .status("new")
                .timestamp(LocalDateTime.now().format(formatter))
                .relatedId(res.getId().toString())
                .relatedType("RESERVATION")
                .actionUrl("/app/reservations")
                .build();
    }
}
