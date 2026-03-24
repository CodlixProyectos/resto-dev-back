package resto_dev.shared.security.sse;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data stored for a temporary SSE ticket.
 */
public record SseTicketData(
    UUID userId,
    UUID organizationId,
    LocalDateTime expiry
) {}
