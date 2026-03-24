package resto_dev.shared.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.adminsaas.subscriptions.infrastructure.persistence.jpa.SubscriptionPlanJpaEntity;
import resto_dev.modules.adminsaas.subscriptions.infrastructure.persistence.jpa.SubscriptionPlanJpaRepository;
import resto_dev.shared.security.permissions.PermissionEntity;
import resto_dev.shared.security.permissions.PermissionRepository;
import resto_dev.shared.security.permissions.RoleEntity;
import resto_dev.shared.security.permissions.RoleRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Seeds initial roles, permissions, and subscription plans on startup.
 * Only runs if data doesn't already exist (idempotent).
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final SubscriptionPlanJpaRepository planRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedPermissions();
        seedRoles();
        seedPlans();
    }

    private void seedPermissions() {
        List<PermissionEntity> permsToSeed = List.of(
                perm("MANAGE_RESTAURANT", "Configurar restaurante", "restaurant"),
                perm("MANAGE_MEMBERS", "Gestionar miembros", "restaurant"),
                perm("VIEW_MENU", "Ver menú", "menu"),
                perm("MANAGE_MENU", "Crear/editar menú", "menu"),
                perm("CREATE_ORDER", "Crear pedidos", "orders"),
                perm("VIEW_ORDERS", "Ver pedidos", "orders"),
                perm("MANAGE_ORDERS", "Gestionar pedidos", "orders"),
                perm("PROCESS_PAYMENT", "Procesar pagos", "payments"),
                perm("VIEW_REPORTS", "Ver reportes", "reports"),
                perm("MANAGE_CASH", "Gestionar caja", "payments"),
                perm("VIEW_KITCHEN", "Ver cocina", "kitchen"),
                perm("UPDATE_KITCHEN", "Actualizar pedidos en cocina", "kitchen"),
                perm("MANAGE_TABLES", "Gestionar mesas", "restaurant"));

        for (PermissionEntity p : permsToSeed) {
            if (permissionRepository.findByCode(p.getCode()).isEmpty()) {
                permissionRepository.save(p);
                log.info("✨ Added missing permission: {}", p.getCode());
            }
        }
    }

    private void seedRoles() {
        List<PermissionEntity> allItems = permissionRepository.findAll();

        // 1. OWNER — Always ensure all permissions
        upsertRole("OWNER", "Dueño del restaurante", true, allItems);

        // 2. MANAGER — everything except governance
        upsertRole("MANAGER", "Administrador de operaciones", true,
                allItems.stream()
                        .filter(p -> !p.getCode().equals("MANAGE_RESTAURANT") && !p.getCode().equals("MANAGE_MEMBERS"))
                        .toList());

        // 3. WAITER
        upsertRole("WAITER", "Mesero", true,
                allItems.stream()
                        .filter(p -> Set.of("VIEW_MENU", "CREATE_ORDER", "VIEW_ORDERS", "PROCESS_PAYMENT", "MANAGE_TABLES",
                                        "VIEW_KITCHEN", "UPDATE_KITCHEN")
                                .contains(p.getCode()))
                        .toList());

        // 4. KITCHEN
        upsertRole("KITCHEN", "Cocina", true,
                allItems.stream()
                        .filter(p -> Set.of("VIEW_KITCHEN", "UPDATE_KITCHEN", "VIEW_ORDERS", "VIEW_MENU")
                                .contains(p.getCode()))
                        .toList());

        log.info("✅ Roles synchronized with latest permissions");
    }

    private void upsertRole(String name, String description, boolean isSystem, List<PermissionEntity> perms) {
        RoleEntity role = roleRepository.findByName(name).orElse(
                RoleEntity.builder()
                        .name(name)
                        .description(description)
                        .system(isSystem)
                        .build());

        // Update permissions if different
        Set<PermissionEntity> newPerms = perms.stream().collect(Collectors.toSet());
        if (!newPerms.equals(role.getPermissions())) {
            role.setPermissions(newPerms);
            roleRepository.save(role);
            log.info("🔄 Updated permissions for role: {}", name);
        }
    }

    private void seedPlans() {
        if (planRepository.count() > 0)
            return;

        planRepository.saveAll(List.of(
                SubscriptionPlanJpaEntity.builder()
                        .name("Free")
                        .description("Plan gratuito para empezar")
                        .maxTables(5).maxUsers(3)
                        .priceMonthly(BigDecimal.ZERO).build(),
                SubscriptionPlanJpaEntity.builder()
                        .name("Pro")
                        .description("Para restaurantes en crecimiento")
                        .maxTables(20).maxUsers(10)
                        .priceMonthly(new BigDecimal("29.99")).build(),
                SubscriptionPlanJpaEntity.builder()
                        .name("Enterprise")
                        .description("Sin límites, soporte premium")
                        .maxTables(999).maxUsers(999)
                        .priceMonthly(new BigDecimal("99.99")).build()));
        log.info("✅ Seeded 3 subscription plans");
    }

    // ── Helpers ──

    private PermissionEntity perm(String code, String desc, String module) {
        return PermissionEntity.builder().code(code).description(desc).module(module).build();
    }
}
