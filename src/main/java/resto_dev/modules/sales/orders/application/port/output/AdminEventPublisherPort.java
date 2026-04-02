package resto_dev.modules.sales.orders.application.port.output;

import resto_dev.modules.sales.orders.infrastructure.web.dto.output.AdminNotificationResponse;

import java.util.UUID;

public interface AdminEventPublisherPort {
    void notifyAdmin(UUID organizationId, AdminNotificationResponse notification, String eventType);
}
