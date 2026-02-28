package resto_dev.modules.adminsaas.members.infrastructure.persistence.jpa;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import resto_dev.modules.adminsaas.members.domain.OrganizationMember;
import resto_dev.modules.adminsaas.members.ports.out.MemberRepositoryPort;
import resto_dev.shared.security.permissions.PermissionEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrganizationMemberRepositoryAdapter implements MemberRepositoryPort {

    private final OrganizationMemberJpaRepository jpaRepository;

    @Override
    public Optional<OrganizationMember> findByOrganizationAndUser(UUID organizationId, UUID userId) {
        return jpaRepository.findByOrganizationIdAndUserId(organizationId, userId).stream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public List<String> findPermissionsByOrganizationAndUser(UUID organizationId, UUID userId) {
        return jpaRepository.findByOrganizationIdAndUserId(organizationId, userId).stream()
                .filter(OrganizationMemberJpaEntity::isActive)
                .flatMap(member -> member.getRole().getPermissions().stream())
                .map(PermissionEntity::getCode)
                .distinct()
                .toList();
    }

    private OrganizationMember toDomain(OrganizationMemberJpaEntity entity) {
        return OrganizationMember.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganization().getId())
                .userId(entity.getUser().getId())
                .roleId(entity.getRole().getId())
                .active(entity.isActive())
                .joinedAt(entity.getJoinedAt())
                .build();
    }
}
