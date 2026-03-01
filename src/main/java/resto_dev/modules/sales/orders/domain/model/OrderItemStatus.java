package resto_dev.modules.sales.orders.domain.model;

public enum OrderItemStatus {
    PENDING, // Esperando en cola
    PREPARING, // Cocinero lo está haciendo
    READY, // Terminado por el cocinero
    SERVED, // Mesero lo entregó
    CANCELLED
}
