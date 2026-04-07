package resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalActiveOrganizations;
    private BigDecimal monthlyRecurringRevenue;
    private long premiumPlansCount;
    private int pendingTickets;
    private List<RecentActivityDto> recentActivity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivityDto {
        private String type; // e.g., "REGISTRATION", "SUBSCRIPTION_UPGRADE"
        private String description;
        private String timeAgo;
    }
}
