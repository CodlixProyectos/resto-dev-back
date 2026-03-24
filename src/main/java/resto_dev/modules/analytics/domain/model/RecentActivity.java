package resto_dev.modules.analytics.domain.model;

import java.time.LocalDateTime;

public record RecentActivity(
        String id,
        String title,
        String subtitle,
        LocalDateTime time,
        String icon,
        String status) {
}
