package resto_dev.modules.adminsaas.organizations.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.output.OrganizationResponse;
import resto_dev.modules.adminsaas.organizations.domain.model.Organization;
import resto_dev.modules.adminsaas.organizations.application.port.input.CreateOrganizationUseCase;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.input.CreateOrganizationRequest;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.input.UpdateOrganizationRequest;
import resto_dev.modules.adminsaas.organizations.application.port.input.UpdateOrganizationUseCase;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.mapper.OrganizationWebMapper;
import resto_dev.modules.adminsaas.organizations.application.port.output.OrganizationRepositoryPort;
import resto_dev.modules.adminsaas.organizations.application.port.input.GetOrganizationsUseCase;
import resto_dev.modules.adminsaas.organizations.application.query.GetOrganizationsQuery;
import resto_dev.shared.responses.ApiResponse;
import resto_dev.shared.responses.PaginatedResponse;
import resto_dev.shared.errors.ResourceNotFoundException;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.output.SaasOrganizationDetailResponse;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.output.SaasOrganizationResponse;
import resto_dev.modules.adminsaas.members.application.port.output.MemberRepositoryPort;
import resto_dev.modules.adminsaas.members.infrastructure.web.dto.output.OrganizationMemberResponse;
import resto_dev.modules.adminsaas.subscriptions.infrastructure.persistence.jpa.SubscriptionPlanJpaRepository;
import resto_dev.modules.adminsaas.subscriptions.infrastructure.persistence.jpa.SubscriptionPlanJpaEntity;
import resto_dev.modules.adminsaas.subscriptions.infrastructure.persistence.jpa.OrganizationSubscriptionJpaRepository;
import resto_dev.modules.adminsaas.subscriptions.infrastructure.persistence.jpa.OrganizationSubscriptionJpaEntity;
import resto_dev.modules.adminsaas.users.application.port.output.UserRepositoryPort;
import resto_dev.shared.security.permissions.RoleRepository;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.input.UpdateSaasSettingsRequest;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.repository.OrganizationJpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Organization REST controller for SaaS administration.
 */
@RestController
@RequestMapping("/api/v1/saas/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizations", description = "Gestión de organizaciones (SaaS Admin)")
public class OrganizationController {

        private final CreateOrganizationUseCase createOrganizationPort;
        private final OrganizationWebMapper webMapper;
        private final GetOrganizationsUseCase getOrganizationsUseCase;
        private final UpdateOrganizationUseCase updateOrganizationUseCase;
        private final OrganizationRepositoryPort organizationRepository;
        private final OrganizationJpaRepository organizationJpaRepository;
        private final SubscriptionPlanJpaRepository subscriptionPlanRepository;
        private final OrganizationSubscriptionJpaRepository subscriptionRepository;
        private final MemberRepositoryPort memberRepository;
        private final UserRepositoryPort userRepository;
        private final RoleRepository roleRepository;

        @Operation(summary = "Listar planes de suscripción", description = "Devuelve los planes disponibles")
        @GetMapping("/plans")
        public ResponseEntity<ApiResponse<List<SubscriptionPlanJpaEntity>>> getPlans() {
                return ResponseEntity.ok(ApiResponse.ok(subscriptionPlanRepository.findAll()));
        }

        @Operation(summary = "Crear organización", description = "Crea una nueva organización")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Creada"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflicto")
        })
        @PostMapping
        public ResponseEntity<ApiResponse<OrganizationResponse>> create(
                        @Valid @RequestBody CreateOrganizationRequest request,
                        @Parameter(hidden = true) @AuthenticationPrincipal UUID superAdminId) {

                var command = webMapper.toCommand(request);
                Organization org = createOrganizationPort.execute(command, superAdminId);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.created(webMapper.toResponse(org), "Organization created"));
        }

        @Operation(summary = "Obtener organización por ID (Detalle completo)")
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<SaasOrganizationDetailResponse>> getById(@PathVariable UUID id) {
                var org = organizationRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

                // Fetch subscription details
                var subscription = subscriptionRepository.findFirstByOrganizationIdOrderByCreatedAtDesc(id)
                                .orElse(null);

                // Build SaasOrganizationResponse
                var saasResponse = new SaasOrganizationResponse(
                                org.getId(),
                                org.getName(),
                                org.getSlug(),
                                org.getEmail(),
                                org.getPhone(),
                                org.isActive(),
                                subscription != null ? subscription.getPlan().getId() : null,
                                subscription != null ? subscription.getPlan().getName() : "N/A",
                                subscription != null ? subscription.getStatus() : "INACTIVE",
                                subscription != null ? subscription.getEndDate() : null,
                                subscription != null ? subscription.getStartDate() : null,
                                subscription != null && subscription.getUserLimit() != null ? subscription.getUserLimit() : 5,
                                org.isHasInventory(),
                                org.isHasPensioners(),
                                org.isHasKds(),
                                org.getInvitationCode(),
                                org.getYapeQrUrl(),
                                org.getPlinQrUrl());

                // Fetch members and map them manually
                var members = memberRepository.findAllByOrganization(id).stream()
                                .map(m -> {
                                        var user = userRepository.findById(m.getUserId()).orElse(null);
                                        var role = roleRepository.findById(m.getRoleId()).orElse(null);

                                        return new OrganizationMemberResponse(
                                                        m.getId(),
                                                        m.getOrganizationId(),
                                                        m.getUserId(),
                                                        m.getRoleId(),
                                                        user != null ? user.getFullName() : "Unknown",
                                                        user != null ? user.getEmail() : "N/A",
                                                        user != null ? user.getPhoneNumber() : "N/A",
                                                        user != null ? user.getDni() : "N/A",
                                                        role != null ? role.getName() : "N/A",
                                                        m.isActive(),
                                                        m.getStatus(),
                                                        m.getSalary(),
                                                        m.getJoinedAt());
                                })
                                .toList();

                return ResponseEntity.ok(ApiResponse.ok(new SaasOrganizationDetailResponse(saasResponse, members)));
        }

        @Operation(summary = "Listar organizaciones paginadas")
        @GetMapping
        public ResponseEntity<ApiResponse<PaginatedResponse<SaasOrganizationResponse>>> getOrganizations(
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "10") int limit,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false, name = "active") Boolean isActive) {

                GetOrganizationsQuery query = GetOrganizationsQuery.builder()
                                .page(page)
                                .size(limit)
                                .search(search)
                                .isActive(isActive)
                                .build();

                PaginatedResponse<Organization> result = getOrganizationsUseCase.getOrganizations(query);

                List<SaasOrganizationResponse> enrichedData = result.getData().stream().map(org -> {
                        // Fetch subscription details
                        var subscription = subscriptionRepository.findFirstByOrganizationIdOrderByCreatedAtDesc(org.getId())
                                        .orElse(null);

                        return new SaasOrganizationResponse(
                                        org.getId(),
                                        org.getName(),
                                        org.getSlug(),
                                        org.getEmail(),
                                        org.getPhone(),
                                        org.isActive(),
                                        subscription != null ? subscription.getPlan().getId() : null,
                                        subscription != null ? subscription.getPlan().getName() : "Plan Básico",
                                        subscription != null ? subscription.getStatus() : "INACTIVE",
                                        subscription != null ? subscription.getEndDate() : null,
                                        subscription != null ? subscription.getStartDate() : null,
                                        subscription != null && subscription.getUserLimit() != null ? subscription.getUserLimit() : 5,
                                        org.isHasInventory(),
                                        org.isHasPensioners(),
                                        org.isHasKds(),
                                        org.getInvitationCode(),
                                        org.getYapeQrUrl(),
                                        org.getPlinQrUrl());
                }).toList();

                PaginatedResponse<SaasOrganizationResponse> response = PaginatedResponse.<SaasOrganizationResponse>builder()
                                .data(enrichedData)
                                .page(result.getPage())
                                .size(result.getSize())
                                .totalElements(result.getTotalElements())
                                .totalPages(result.getTotalPages())
                                .hasNext(result.isHasNext())
                                .hasPrevious(result.isHasPrevious())
                                .build();

                return ResponseEntity.ok(ApiResponse.ok(response));
        }

        @Operation(summary = "Actualizar organización (General)")
        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<OrganizationResponse>> update(
                        @PathVariable UUID id,
                        @Valid @RequestBody UpdateOrganizationRequest request) {
                var command = new UpdateOrganizationUseCase.UpdateOrganizationCommand(
                                request.name(),
                                request.legalName(),
                                request.businessId(),
                                request.email(),
                                request.phone(),
                                request.address(),
                                request.logoUrl(),
                                request.primaryColor(),
                                request.secondaryColor(),
                                request.sunatUser(),
                                request.sunatPassword(),
                                request.sunatClientId(),
                                request.sunatClientSecret(),
                                request.yapeQrUrl(),
                                request.plinQrUrl());

                Organization updated = updateOrganizationUseCase.execute(id, command);
                return ResponseEntity.ok(ApiResponse.ok(webMapper.toResponse(updated), "Organization updated"));
        }

        @Operation(summary = "Obtener miembros por ID de organización")
        @GetMapping("/{id}/members")
        public ResponseEntity<ApiResponse<List<OrganizationMemberResponse>>> getMembers(@PathVariable UUID id) {
                var members = memberRepository.findAllByOrganization(id).stream()
                                .map(m -> {
                                        var user = userRepository.findById(m.getUserId()).orElse(null);
                                        var role = roleRepository.findById(m.getRoleId()).orElse(null);

                                        return new OrganizationMemberResponse(
                                                        m.getId(),
                                                        m.getOrganizationId(),
                                                        m.getUserId(),
                                                        m.getRoleId(),
                                                        user != null ? user.getFullName() : "Unknown",
                                                        user != null ? user.getEmail() : "N/A",
                                                        user != null ? user.getPhoneNumber() : "N/A",
                                                        user != null ? user.getDni() : "N/A",
                                                        role != null ? role.getName() : "N/A",
                                                        m.isActive(),
                                                        m.getStatus(),
                                                        m.getSalary(),
                                                        m.getJoinedAt());
                                })
                                .toList();
                return ResponseEntity.ok(ApiResponse.ok(members));
        }

        @Operation(summary = "Actualizar configuración SaaS")
        @PutMapping("/{id}/settings")
        public ResponseEntity<ApiResponse<String>> updateSettings(
                        @PathVariable UUID id,
                        @Valid @RequestBody UpdateSaasSettingsRequest request) {

                Organization org = organizationRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

                // Update organization flags
                org.setHasInventory(request.isHasInventory());
                org.setHasPensioners(request.isHasPensioners());
                org.setHasKds(request.isHasKds());
                organizationRepository.save(org);

                // Update or create subscription
                var subscription = subscriptionRepository.findFirstByOrganizationIdOrderByCreatedAtDesc(id)
                                .orElseGet(() -> {
                                        var newSub = new OrganizationSubscriptionJpaEntity();
                                        // Need JPA entity handle
                                        var jpaOrg = organizationJpaRepository.findById(id).orElseThrow();
                                        newSub.setOrganization(jpaOrg);
                                        newSub.setStartDate(LocalDate.now());
                                        return newSub;
                                });

                if (request.getPlanId() != null) {
                        subscriptionPlanRepository.findById(request.getPlanId())
                                        .ifPresent(subscription::setPlan);
                }
                subscription.setStatus(request.getSubscriptionStatus());
                if (request.getEndDate() != null) {
                        subscription.setEndDate(request.getEndDate());
                }
                subscription.setUserLimit(request.getUserLimit());

                subscriptionRepository.save(subscription);

                return ResponseEntity.ok(ApiResponse.ok("Settings updated successfully"));
        }

        @Operation(summary = "Desactivar organización")
        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<String>> deactivate(@PathVariable UUID id) {
                Organization org = organizationRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
                org.setActive(false);
                organizationRepository.save(org);
                return ResponseEntity.ok(ApiResponse.ok("Organization deactivated"));
        }

        @Operation(summary = "Eliminar organización permanentemente")
        @DeleteMapping("/{id}/force")
        public ResponseEntity<ApiResponse<String>> forceDelete(@PathVariable UUID id) {
                Organization org = organizationRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
                // Simply deactivate for now to be safe
                org.setActive(false);
                organizationRepository.save(org);
                return ResponseEntity.ok(ApiResponse.ok("Organization marked for deletion (simulated)"));
        }

        @Operation(summary = "Regenerar código de invitación")
        @PostMapping("/{id}/invitation-code/refresh")
        public ResponseEntity<ApiResponse<String>> refreshInvitationCode(@PathVariable UUID id) {
                Organization org = organizationRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
                
                String newCode = generateRandomCode();
                org.setInvitationCode(newCode);
                organizationRepository.save(org);
                
                return ResponseEntity.ok(ApiResponse.ok(newCode, "Invitation code refreshed"));
        }

        private String generateRandomCode() {
                String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
                StringBuilder sb = new StringBuilder("RT-");
                Random rnd = new Random();
                for (int i = 0; i < 6; i++) {
                        sb.append(chars.charAt(rnd.nextInt(chars.length())));
                }
                return sb.toString();
        }
}
