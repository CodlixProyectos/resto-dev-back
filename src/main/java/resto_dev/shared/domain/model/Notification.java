package resto_dev.shared.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private String id;
    private String title;
    private String message;
    private String type; // success, warning, info, message
    private String status; // new, read
    private LocalDateTime timestamp;
    private String relatedId;
    private String relatedType;
    private String actionUrl;
}
