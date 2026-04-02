package resto_dev.modules.sales.orders.infrastructure.web.dto.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminNotificationResponse {
    private String id;
    private String title;
    private String message;
    private String type; // success, warning, info, message
    private String status; // new, read
    private String timestamp;
    private String relatedId;
    private String relatedType;
    private String actionUrl;
}
