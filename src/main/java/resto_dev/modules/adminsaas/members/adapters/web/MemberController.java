package resto_dev.modules.adminsaas.members.adapters.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import resto_dev.modules.adminsaas.members.ports.in.GetMyPermissionsPort;
import resto_dev.shared.responses.ApiResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/my-permissions")
@RequiredArgsConstructor
@Tag(name = "Organization Members", description = "Endpoints para gestionar miembros y permisos dentro de una organización")
public class MemberController {

    private final GetMyPermissionsPort getMyPermissionsPort;

    @Operation(summary = "Obtener mis permisos", description = "Devuelve una lista de códigos de permisos (ej. CREATE_ORDER, VIEW_MENU) que tiene el usuario autenticado dentro de la organización especificada.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Permisos obtenidos exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin acceso a la organización")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<String>>> getMyPermissions(
            @Parameter(description = "ID de la organización", required = true) @PathVariable UUID organizationId,
            @Parameter(hidden = true) @AuthenticationPrincipal UUID userId) {

        List<String> permissions = getMyPermissionsPort.execute(organizationId, userId);
        return ResponseEntity.ok(ApiResponse.ok(permissions));
    }
}
