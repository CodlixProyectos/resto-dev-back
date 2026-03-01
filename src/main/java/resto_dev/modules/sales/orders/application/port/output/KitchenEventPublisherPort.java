package resto_dev.modules.sales.orders.application.port.output;

import resto_dev.modules.sales.orders.domain.model.Order;

import java.util.UUID;

/**
 * Puerto de salida para notificar eventos de las órdenes en tiempo real a la
 * cocina.
 */
public interface KitchenEventPublisherPort {

    /**
     * Publica un evento avisando que una orden fue creada o actualizada en la
     * cocina.
     * 
     * @param organizationId ID de la organización (tenant).
     * @param order          La orden afectada.
     * @param eventType      Cadena describiendo el evento (e.g., "ORDER_CREATED",
     *                       "ORDER_UPDATED").
     */
    void publishOrderEvent(UUID organizationId, Order order, String eventType);
}
