package resto_dev.modules.adminsaas.organizations.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.adminsaas.organizations.application.service.InvitationCodeApplicationService;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.input.GenerateInvitationCodeRequest;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.output.InvitationCodeResponse;
import resto_dev.shared.responses.ApiResponse;
import resto_dev.shared.responses.PaginatedResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/saas/invitation-codes")
@RequiredArgsConstructor
@Tag(name = "Invitation Codes", description = "Gestión de códigos de invitación (Licencias)")
public class InvitationCodeController {

    private final InvitationCodeApplicationService invitationCodeService;

    @Operation(summary = "Listar todos los códigos de invitación con paginación")
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<InvitationCodeResponse>>> getAll(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        var page = invitationCodeService.getAllCodes(pageable);
        return ResponseEntity.ok(ApiResponse.ok(PaginatedResponse.of(page)));
    }

    @Operation(summary = "Generar un nuevo código de invitación")
    @PostMapping
    public ResponseEntity<ApiResponse<InvitationCodeResponse>> generate(@Valid @RequestBody GenerateInvitationCodeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(invitationCodeService.generateCode(request), "Código generado correctamente"));
    }

    @Operation(summary = "Eliminar un código de invitación")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        invitationCodeService.deleteCode(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Código eliminado correctamente"));
    }
}
