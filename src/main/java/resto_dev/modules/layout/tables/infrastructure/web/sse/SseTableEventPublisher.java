package resto_dev.modules.layout.tables.infrastructure.web.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import resto_dev.modules.layout.tables.application.port.output.TableEventPublisherPort;
import resto_dev.modules.layout.tables.domain.model.Table;
import resto_dev.modules.layout.tables.infrastructure.web.dto.output.TableEventResponse;
import resto_dev.modules.layout.tables.infrastructure.web.mapper.TableEventMapper;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseTableEventPublisher implements TableEventPublisherPort {

    private final ConcurrentHashMap<UUID, List<SseEmitter>> organizationEmitters = new ConcurrentHashMap<>();
    private final TableEventMapper tableEventMapper;

    public SseEmitter subscribe(UUID organizationId) {
        SseEmitter emitter = new SseEmitter(60L * 60L * 1000L); // 1 hour timeout

        organizationEmitters.computeIfAbsent(organizationId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(organizationId, emitter));
        emitter.onTimeout(() -> removeEmitter(organizationId, emitter));
        emitter.onError((e) -> removeEmitter(organizationId, emitter));

        log.debug("Nuevo suscriptor a eventos de mesas para org: {}. Activos: {}", 
            organizationId, organizationEmitters.get(organizationId).size());

        try {
            emitter.send(SseEmitter.event().name("CONNECT").data("Canal de Mesas Activo"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    private void removeEmitter(UUID organizationId, SseEmitter emitter) {
        List<SseEmitter> emitters = organizationEmitters.get(organizationId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                organizationEmitters.remove(organizationId);
            }
        }
    }

    @Override
    public void publishTableEvent(UUID organizationId, Table table, String eventType) {
        List<SseEmitter> emitters = organizationEmitters.get(organizationId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        TableEventResponse payload = tableEventMapper.toEventResponse(table);
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(payload));
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        });

        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
        }
    }

    @Scheduled(fixedRate = 20000)
    public void scheduledHeartbeat() {
        if (organizationEmitters.isEmpty()) return;

        organizationEmitters.forEach((orgId, emittersList) -> {
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
            }
        });
    }
}
