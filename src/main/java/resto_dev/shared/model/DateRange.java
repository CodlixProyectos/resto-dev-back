package resto_dev.shared.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DateRange {
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public static DateRange of(LocalDateTime start, LocalDateTime end) {
        return new DateRange(start, end);
    }
}
