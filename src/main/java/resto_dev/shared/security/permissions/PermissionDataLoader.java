package resto_dev.shared.security.permissions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Initializes permissions in the database on application startup.
 * Only creates permissions if they don't exist (idempotent).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionDataLoader implements CommandLineRunner {

    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (permissionRepository.count() > 0) {
            log.debug("Permissions already exist, skipping initialization");
            return;
        }

        log.info("🔐 Initializing default permissions...");

        List<PermissionEntity> permissions = List.of(
                // Layout permissions
                createPermission("VIEW_LAYOUT", "View zones and tables", "Layout"),
                createPermission("MANAGE_LAYOUT", "Create/update/delete zones and tables", "Layout"),
                
                // Menu permissions
                createPermission("VIEW_MENU", "View menu items and categories", "Menu"),
                createPermission("MANAGE_MENU", "Create/update/delete menu items", "Menu"),
                
                // Order permissions
                createPermission("VIEW_ORDERS", "View all orders", "Orders"),
                createPermission("MANAGE_ORDERS", "Create/update/delete orders", "Orders"),
                createPermission("CREATE_ORDER", "Create new orders", "Orders"),
                createPermission("CANCEL_ORDER", "Cancel orders", "Orders"),
                
                // Kitchen permissions
                createPermission("VIEW_KITCHEN", "View kitchen orders", "Kitchen"),
                createPermission("UPDATE_KITCHEN", "Update order status in kitchen", "Kitchen"),
                
                // Staff permissions
                createPermission("VIEW_STAFF", "View staff members", "Staff"),
                createPermission("MANAGE_STAFF", "Add/remove staff members", "Staff"),
                
                // Reports permissions
                createPermission("VIEW_REPORTS", "View sales and analytics reports", "Reports"),
                createPermission("EXPORT_REPORTS", "Export reports to PDF/Excel", "Reports"),
                
                // Settings permissions
                createPermission("VIEW_SETTINGS", "View organization settings", "Settings"),
                createPermission("MANAGE_SETTINGS", "Update organization settings", "Settings"),
                
                // Reservations permissions
                createPermission("VIEW_RESERVATIONS", "View reservations", "Reservations"),
                createPermission("MANAGE_RESERVATIONS", "Create/update/delete reservations", "Reservations")
        );

        permissionRepository.saveAll(permissions);
        log.info("✅ {} permissions initialized successfully", permissions.size());
    }

    private PermissionEntity createPermission(String code, String description, String module) {
        return PermissionEntity.builder()
                .code(code)
                .description(description)
                .module(module)
                .build();
    }
}
