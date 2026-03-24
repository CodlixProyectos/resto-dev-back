package resto_dev.modules.sales.orders.application.port.output;

import resto_dev.modules.sales.orders.domain.model.Order;
import java.util.UUID;

/**
 * Puerto de salida para notificar eventos de las órdenes en tiempo real a los meseros.
 */
public interface WaiterEventPublisherPort {

    /**
     * Publica un evento a un mesero específico.
     * 
     * @param organizationId ID de la organización.
     * @param waiterId       ID del mesero a notificar.
     * @param order          La orden afectada.
     * @param eventType      Tipo de evento (ORDER_READY, etc.).
     */
    void notifyWaiter(UUID organizationId, UUID waiterId, Order order, String eventType);
}
