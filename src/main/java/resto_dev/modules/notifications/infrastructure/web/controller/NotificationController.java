package resto_dev.modules.notifications.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.notifications.application.service.NotificationAppService;
import resto_dev.modules.notifications.infrastructure.persistence.entity.NotificationJpaEntity;
import resto_dev.shared.common.pagination.PageModel;
import resto_dev.shared.responses.ApiResponse;
import resto_dev.shared.responses.PaginatedResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Endpoints para la gestión y persistencia de notificaciones de administración")
public class NotificationController {

    private final NotificationAppService service;

    @GetMapping
    @Operation(summary = "Listar notificaciones", description = "Obtiene el historial de notificaciones de la organización.")
    public ResponseEntity<ApiResponse<PaginatedResponse<NotificationJpaEntity>>> list(
            @RequestHeader("X-Organization-Id") UUID organizationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<NotificationJpaEntity> pageResult = service.getNotifications(organizationId, PageRequest.of(page, size));
        
        PaginatedResponse<NotificationJpaEntity> response = PaginatedResponse.<NotificationJpaEntity>builder()
                .data(pageResult.getContent())
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .hasNext(pageResult.hasNext())
                .hasPrevious(pageResult.hasPrevious())
                .build();
                
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marcar como leída", description = "Actualiza el estado de una notificación a leída.")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable UUID id) {
        service.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Notificación marcada como leída"));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Marcar todas como leídas", description = "Marca todas las notificaciones de la organización como leídas.")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@RequestHeader("X-Organization-Id") UUID organizationId) {
        service.markAllAsRead(organizationId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Todas las notificaciones marcadas como leídas"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar notificación", description = "Elimina permanentemente una notificación.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Notificación eliminada"));
    }
}
