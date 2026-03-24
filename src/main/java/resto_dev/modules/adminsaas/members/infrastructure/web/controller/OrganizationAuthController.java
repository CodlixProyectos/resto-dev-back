package resto_dev.modules.adminsaas.members.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.adminsaas.members.application.port.input.PinLoginOrganizationMemberUseCase;
import resto_dev.modules.adminsaas.members.infrastructure.web.dto.input.PinLoginRequest;
import resto_dev.modules.adminsaas.users.application.command.AuthResult;
import resto_dev.shared.responses.ApiResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Tag(name = "Organization Authentication", description = "Endpoints para autenticación global de organizaciones (POS Login).")
public class OrganizationAuthController {

    private final PinLoginOrganizationMemberUseCase pinLoginUseCase;

    @Operation(summary = "POS PIN Login (Global)", 
               description = "Login rápido usando PIN para tablets de Punto de Venta o Cocina, sin especificar organización en la URL.")
    @PostMapping("/auth/pin")
    public ResponseEntity<ApiResponse<AuthResult>> globalPinLogin(@Valid @RequestBody PinLoginRequest request) {
        AuthResult result = pinLoginUseCase.execute(null, request.getEmail(), request.getDni(), request.getPin());
        return ResponseEntity.ok(ApiResponse.ok(result, "POS Global Login Successful"));
    }

    @Operation(summary = "POS PIN Login (Organization Specific)", 
               description = "Login rápido usando PIN especificando la organización en la URL.")
    @PostMapping("/{organizationId}/auth/pin")
    public ResponseEntity<ApiResponse<AuthResult>> organizationPinLogin(
            @PathVariable UUID organizationId,
            @Valid @RequestBody PinLoginRequest request) {
        AuthResult result = pinLoginUseCase.execute(organizationId, request.getEmail(), request.getDni(), request.getPin());
        return ResponseEntity.ok(ApiResponse.ok(result, "POS Login Successful"));
    }
}
