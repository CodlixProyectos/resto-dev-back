package resto_dev.modules.sales.orders.infrastructure.web.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import resto_dev.modules.sales.orders.application.port.output.AdminEventPublisherPort;
import resto_dev.modules.sales.orders.infrastructure.web.dto.output.AdminNotificationResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import resto_dev.modules.notifications.application.service.NotificationAppService;
import resto_dev.modules.notifications.infrastructure.persistence.entity.NotificationJpaEntity;

@Slf4j
@Component
@lombok.RequiredArgsConstructor
public class SseAdminEventPublisher implements AdminEventPublisherPort {

    private final NotificationAppService notificationAppService;

    // Mapa: OrganizationID -> Lista de Suscripciones (Administradores)
    private final ConcurrentHashMap<UUID, List<SseEmitter>> orgAdminsEmitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(UUID organizationId) {
        // Timeout de 1 hora
        SseEmitter emitter = new SseEmitter(60L * 60L * 1000L);

        orgAdminsEmitters.computeIfAbsent(organizationId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(organizationId, emitter));
        emitter.onTimeout(() -> removeEmitter(organizationId, emitter));
        emitter.onError((e) -> removeEmitter(organizationId, emitter));

        log.debug("Administrador suscrito a organización: {}. Total conexiones activas: {}", 
                organizationId, orgAdminsEmitters.get(organizationId).size());

        try {
            emitter.send(SseEmitter.event().name("CONNECT").data("Conexión de notificaciones para administrador activa"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    private void removeEmitter(UUID organizationId, SseEmitter emitter) {
        List<SseEmitter> emitters = orgAdminsEmitters.get(organizationId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                orgAdminsEmitters.remove(organizationId);
            }
        }
    }

    @Override
    public void notifyAdmin(UUID organizationId, AdminNotificationResponse notification, String eventType) {
        // Save to Database for persistence
        try {
            NotificationJpaEntity entity = NotificationJpaEntity.builder()
                    .id(java.util.UUID.fromString(notification.getId()))
                    .organizationId(organizationId)
                    .title(notification.getTitle())
                    .message(notification.getMessage())
                    .type(notification.getType())
                    .relatedId(notification.getRelatedId())
                    .relatedType(notification.getRelatedType())
                    .actionUrl(notification.getActionUrl())
                    .createdAt(java.time.LocalDateTime.parse(notification.getTimestamp()))
                    .read(false)
                    .build();
            notificationAppService.save(entity);
        } catch (Exception e) {
            log.error("Error persisting notification to database: {}", e.getMessage());
            // We continue with broadcasting even if saving fails to keep real-time UI fast
        }

        List<SseEmitter> emitters = orgAdminsEmitters.get(organizationId);
        if (emitters == null || emitters.isEmpty()) {
            log.debug("No hay administradores conectados para la organización {}, evento {} reservado.", organizationId, eventType);
            return;
        }

        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(notification));
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
        if (orgAdminsEmitters.isEmpty()) return;

        orgAdminsEmitters.forEach((orgId, emittersList) -> {
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
                    orgAdminsEmitters.remove(orgId);
                }
            }
        });
    }
}
