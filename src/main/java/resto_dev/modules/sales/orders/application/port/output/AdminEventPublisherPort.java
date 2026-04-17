package resto_dev.modules.sales.orders.application.port.output;

import resto_dev.shared.domain.model.Notification;

import java.util.UUID;

public interface AdminEventPublisherPort {
    void notifyAdmin(UUID organizationId, Notification notification, String eventType);
}
