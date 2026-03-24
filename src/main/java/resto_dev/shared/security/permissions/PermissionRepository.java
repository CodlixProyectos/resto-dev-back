package resto_dev.shared.security.permissions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, UUID> {
    
    Optional<PermissionEntity> findByCode(String code);
    
    boolean existsByCode(String code);
}
