package resto_dev.modules.reservations.infrastructure.api.dto.output;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationStatsResponse {
    private long totalReservations;
    private long pendingCount;
    private long confirmedCount;
    private long seatedCount;
}
