package resto_dev.modules.sales.orders.infrastructure.web.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import resto_dev.modules.sales.orders.application.port.output.WaiterEventPublisherPort;
import resto_dev.modules.sales.orders.domain.model.Order;
import resto_dev.modules.sales.orders.infrastructure.web.dto.output.OrderResponse;
import resto_dev.modules.sales.orders.infrastructure.web.mapper.OrderWebMapper;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Component
public class SseWaiterEventPublisher implements WaiterEventPublisherPort {

    // Mapa: WaiterID -> Lista de Suscripciones (puede tener varias pestañas abiertas)
    private final ConcurrentHashMap<UUID, List<SseEmitter>> waitersEmitters = new ConcurrentHashMap<>();
    private final OrderWebMapper webMapper;

    public SseWaiterEventPublisher(OrderWebMapper webMapper) {
        this.webMapper = webMapper;
    }

    public SseEmitter subscribe(UUID waiterId) {
        // Timeout de 1 hora
        SseEmitter emitter = new SseEmitter(60L * 60L * 1000L);

        waitersEmitters.computeIfAbsent(waiterId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(waiterId, emitter));
        emitter.onTimeout(() -> removeEmitter(waiterId, emitter));
        emitter.onError((e) -> removeEmitter(waiterId, emitter));

        log.debug("Mesero suscrito: {}. Total conexiones activas: {}", waiterId, waitersEmitters.get(waiterId).size());

        try {
            emitter.send(SseEmitter.event().name("CONNECT").data("Conexión de tiempo real activa"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    private void removeEmitter(UUID waiterId, SseEmitter emitter) {
        List<SseEmitter> orgEmitters = waitersEmitters.get(waiterId);
        if (orgEmitters != null) {
            orgEmitters.remove(emitter);
            if (orgEmitters.isEmpty()) {
                waitersEmitters.remove(waiterId);
            }
        }
    }

    @Override
    public void notifyWaiter(UUID organizationId, UUID waiterId, Order order, String eventType) {
        List<SseEmitter> orgEmitters = waitersEmitters.get(waiterId);
        if (orgEmitters == null || orgEmitters.isEmpty()) {
            log.debug("No hay conexiones activas para el mesero {}, evento {} ignorado.", waiterId, eventType);
            return;
        }

        OrderResponse payload = webMapper.toResponse(order);
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        orgEmitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(payload));
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        });

        if (!deadEmitters.isEmpty()) {
            orgEmitters.removeAll(deadEmitters);
        }
    }

    /**
     * Heartbeat every 20 seconds to keep connections alive and detect dead ones early.
     * Essential for multi-tenant environments where idle connections might be dropped by proxies.
     */
    @Scheduled(fixedRate = 20000)
    public void scheduledHeartbeat() {
        if (waitersEmitters.isEmpty()) return;

        log.trace("Sending heartbeat to {} active waiter connections", waitersEmitters.size());
        
        waitersEmitters.forEach((waiterId, emittersList) -> {
            List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
            
            emittersList.forEach(emitter -> {
                try {
                    emitter.send(SseEmitter.event().name("HEARTBEAT").data("💓"));
                } catch (Exception e) {
                    deadEmitters.add(emitter);
                }
            });

            if (!deadEmitters.isEmpty()) {
                emittersList.removeAll(deadEmitters);
                if (emittersList.isEmpty()) {
                    waitersEmitters.remove(waiterId);
                }
            }
        });
    }
}
