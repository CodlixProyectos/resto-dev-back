package resto_dev.modules.adminsaas.users.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.adminsaas.users.infrastructure.web.mapper.UserWebMapper;
import resto_dev.modules.adminsaas.users.infrastructure.web.dto.input.LoginRequest;
import resto_dev.modules.adminsaas.users.infrastructure.web.dto.input.RegisterRequest;
import resto_dev.modules.adminsaas.users.infrastructure.web.dto.output.UserResponse;
import resto_dev.modules.adminsaas.users.application.service.GetUserProfileApplicationService;
import resto_dev.modules.adminsaas.users.domain.model.User;
import resto_dev.modules.adminsaas.users.application.port.input.LoginUserUseCase;
import resto_dev.modules.adminsaas.users.application.port.input.RegisterUserUseCase;
import resto_dev.modules.adminsaas.users.application.command.AuthResult;
import resto_dev.modules.adminsaas.users.application.command.LoginCommand;
import resto_dev.shared.responses.ApiResponse;

import java.util.UUID;

/**
 * Auth REST controller — handles registration, login, and profile.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticación y gestión de usuarios")
public class AuthController {

        private final RegisterUserUseCase registerUserPort;
        private final LoginUserUseCase loginUserPort;
        private final GetUserProfileApplicationService getUserProfileUseCase;
        private final UserWebMapper userDtoMapper;

        @Operation(summary = "Registrar usuario", description = "Crea una nueva cuenta de usuario con email, contraseña y rol.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email ya registrado")
        })
        @SecurityRequirement(name = "")
        @PostMapping("/register")
        public ResponseEntity<ApiResponse<UserResponse>> register(
                        @Valid @RequestBody RegisterRequest request) {
                User user = registerUserPort.execute(userDtoMapper.toCommand(request));
                UserResponse response = userDtoMapper.toResponse(user);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiResponse.created(response, "User registered successfully"));
        }

        @Operation(summary = "Iniciar sesión", description = "Autentica al usuario y devuelve un JWT token.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login exitoso, devuelve JWT"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciales inválidas")
        })
        @SecurityRequirement(name = "")
        @PostMapping("/login")
        public ResponseEntity<ApiResponse<AuthResult>> login(
                        @Valid @RequestBody LoginRequest request) {
                AuthResult result = loginUserPort.execute(
                                new LoginCommand(request.email(), request.password()));

                return ResponseEntity.ok(ApiResponse.ok(result, "Login successful"));
        }

        @Operation(summary = "Obtener perfil", description = "Devuelve el perfil del usuario autenticado. Requiere JWT Bearer token.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Perfil del usuario"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado")
        })
        @GetMapping("/me")
        public ResponseEntity<ApiResponse<UserResponse>> me(
                        @Parameter(hidden = true) @AuthenticationPrincipal UUID userId) {
                User user = getUserProfileUseCase.execute(userId);
                UserResponse response = userDtoMapper.toResponse(user);

                return ResponseEntity.ok(ApiResponse.ok(response));
        }
}
