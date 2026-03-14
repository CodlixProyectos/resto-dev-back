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
import resto_dev.modules.adminsaas.users.application.port.input.DeleteUserAccountUseCase;
import resto_dev.modules.adminsaas.users.application.port.input.UpdateUserPasswordUseCase;
import resto_dev.modules.adminsaas.users.application.port.input.UpdateUserProfileUseCase;
import resto_dev.modules.adminsaas.users.infrastructure.web.dto.input.UpdatePasswordRequest;
import resto_dev.modules.adminsaas.users.infrastructure.web.dto.input.UpdateProfileRequest;
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
        private final UpdateUserProfileUseCase updateUserProfileUseCase;
        private final UpdateUserPasswordUseCase updateUserPasswordUseCase;
        private final DeleteUserAccountUseCase deleteUserAccountUseCase;
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

        @Operation(summary = "Actualizar mi perfil", description = "Actualiza el nombre, teléfono y foto de perfil del usuario logueado.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Perfil actualizado"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado")
        })
        @PutMapping("/me")
        public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
                        @Parameter(hidden = true) @AuthenticationPrincipal UUID userId,
                        @Valid @RequestBody UpdateProfileRequest request) {
                User user = updateUserProfileUseCase.updateUserProfile(
                                userId, request.getFullName(), request.getPhoneNumber(), request.getAvatarUrl());

                return ResponseEntity
                                .ok(ApiResponse.ok(userDtoMapper.toResponse(user), "Perfil actualizado correctamente"));
        }

        @Operation(summary = "Cambiar Contraseña", description = "Permite al usuario autenticado cambiar su contraseña actual.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Contraseña cambiada"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "La contraseña actual es incorrecta o los datos no son válidos"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado")
        })
        @PutMapping("/password")
        public ResponseEntity<ApiResponse<Void>> updatePassword(
                        @Parameter(hidden = true) @AuthenticationPrincipal UUID userId,
                        @Valid @RequestBody UpdatePasswordRequest request) {
                updateUserPasswordUseCase.updateUserPassword(
                                userId, request.getCurrentPassword(), request.getNewPassword());

                return ResponseEntity.ok(ApiResponse.ok(null, "Contraseña actualizada exitosamente"));
        }

        @Operation(summary = "Eliminar Cuenta", description = "Elimina permanentemente la cuenta del usuario actual y todos sus datos personales.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cuenta eliminada"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado")
        })
        @DeleteMapping("/me")
        public ResponseEntity<ApiResponse<Void>> deleteAccount(
                        @Parameter(hidden = true) @AuthenticationPrincipal UUID userId) {
                deleteUserAccountUseCase.deleteUserAccount(userId);

                return ResponseEntity.ok(ApiResponse.ok(null, "Cuenta eliminada permanentemente"));
        }
}
