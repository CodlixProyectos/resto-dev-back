package resto_dev.modules.sales.orders.domain.model;

public enum OrderStatus {
    PENDING_KITCHEN, // Tomado por el mesero, listo para enviarse a cocina
    PREPARING, // La cocina empezó a preparar al menos un ítem
    READY_TO_SERVE, // Todos los ítems terminaron en cocina, esperando que el mesero los recoja
    DELIVERED, // Entregados en la mesa
    PAID, // Pagado y finalizado
    CANCELLED // Orden cancelada
}
