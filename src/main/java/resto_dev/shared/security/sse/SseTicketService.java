package resto_dev.shared.security.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage one-time tickets for SSE authentication.
 * Tickets are valid for a short period (e.g., 60 seconds).
 */
@Slf4j
@Service
public class SseTicketService {

    private final Map<UUID, SseTicketData> tickets = new ConcurrentHashMap<>();
    private static final long TICKET_EXPIRY_SECONDS = 60;

    /**
     * Generates a new one-time ticket.
     */
    public UUID generateTicket(UUID userId, UUID organizationId) {
        UUID ticketId = UUID.randomUUID();
        SseTicketData data = new SseTicketData(
            userId,
            organizationId,
            LocalDateTime.now().plusSeconds(TICKET_EXPIRY_SECONDS)
        );
        tickets.put(ticketId, data);
        log.debug("Generated SSE ticket {} for user {} in org {}", ticketId, userId, organizationId);
        return ticketId;
    }

    /**
     * Validates and consumes a ticket. Returns the associated data if valid.
     */
    public Optional<SseTicketData> validateAndConsumeTicket(UUID ticketId) {
        SseTicketData data = tickets.remove(ticketId);
        
        if (data == null) {
            log.warn("Attempted to use non-existent SSE ticket: {}", ticketId);
            return Optional.empty();
        }

        if (data.expiry().isBefore(LocalDateTime.now())) {
            log.warn("Attempted to use expired SSE ticket: {}", ticketId);
            return Optional.empty();
        }

        log.debug("Validated and consumed SSE ticket {} for user {}", ticketId, data.userId());
        return Optional.of(data);
    }

    /**
     * Cleanup expired tickets (could be called by a scheduler).
     */
    public void cleanup() {
        int initialSize = tickets.size();
        tickets.entrySet().removeIf(entry -> entry.getValue().expiry().isBefore(LocalDateTime.now()));
        int removed = initialSize - tickets.size();
        if (removed > 0) {
            log.debug("Cleaned up {} expired SSE tickets", removed);
        }
    }
}
