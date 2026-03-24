package resto_dev.modules.sales.orders.infrastructure.web.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import resto_dev.modules.sales.orders.application.port.output.KitchenEventPublisherPort;
import resto_dev.modules.sales.orders.domain.model.Order;
import resto_dev.modules.sales.orders.infrastructure.web.dto.output.KitchenOrderResponse;
import resto_dev.modules.sales.orders.infrastructure.web.mapper.OrderWebMapper;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Component
public class SseKitchenEventPublisher implements KitchenEventPublisherPort {

    // Mapa: OrganizationId -> Lista de Clientes conectados (KDS screens)
    private final ConcurrentHashMap<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final OrderWebMapper webMapper;

    public SseKitchenEventPublisher(OrderWebMapper webMapper) {
        this.webMapper = webMapper;
    }

    /**
     * Agrega una nueva pantalla de cocina (cliente) para una organización.
     */
    public SseEmitter subscribe(UUID organizationId) {
        // Timeout de 1 hora para evitar caídas frecuentes.
        SseEmitter emitter = new SseEmitter(60L * 60L * 1000L);

        emitters.computeIfAbsent(organizationId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(organizationId, emitter));
        emitter.onTimeout(() -> removeEmitter(organizationId, emitter));
        emitter.onError((e) -> removeEmitter(organizationId, emitter));

        log.debug("Nuevo KDS suscrito a la organización: {}. Total activos: {}", organizationId,
                emitters.get(organizationId).size());

        // Enviar evento de conexión exitosa
        try {
            emitter.send(SseEmitter.event().name("CONNECT").data("Conexión establecida con KDS"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    private void removeEmitter(UUID organizationId, SseEmitter emitter) {
        List<SseEmitter> orgEmitters = emitters.get(organizationId);
        if (orgEmitters != null) {
            orgEmitters.remove(emitter);
            if (orgEmitters.isEmpty()) {
                emitters.remove(organizationId);
            }
        }
    }

    @Override
    public void publishOrderEvent(UUID organizationId, Order order, String eventType) {
        List<SseEmitter> orgEmitters = emitters.get(organizationId);
        if (orgEmitters == null || orgEmitters.isEmpty()) {
            // No hay nadie escuchando en la cocina de esta organización actualmente
            return;
        }

        // Mapeamos a Response para que el KDS tenga el mismo formato que el REST API
        // normal
        KitchenOrderResponse payload = webMapper.toKitchenResponse(order);

        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        orgEmitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(payload));
            } catch (Exception e) {
                // Si la pantalla se apagó o desconectó sin avisar
                deadEmitters.add(emitter);
            }
        });

        if (!deadEmitters.isEmpty()) {
            orgEmitters.removeAll(deadEmitters);
        }
    }

    /**
     * Heartbeat every 20 seconds to keep KDS connections alive.
     */
    @Scheduled(fixedRate = 20000)
    public void scheduledHeartbeat() {
        if (emitters.isEmpty()) return;

        log.trace("Sending heartbeat to {} active KDS connections", emitters.size());
        
        emitters.forEach((orgId, emittersList) -> {
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
                    emitters.remove(orgId);
                }
            }
        });
    }
}
