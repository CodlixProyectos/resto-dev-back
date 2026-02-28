package resto_dev.modules.adminsaas.members.infrastructure.persistence.adapter;

import resto_dev.modules.adminsaas.members.infrastructure.persistence.entity.OrganizationMemberJpaEntity;
import resto_dev.modules.adminsaas.members.infrastructure.persistence.repository.OrganizationMemberJpaRepository;
import resto_dev.modules.adminsaas.members.infrastructure.persistence.mapper.OrganizationMemberJpaMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import resto_dev.modules.adminsaas.members.domain.model.OrganizationMember;
import resto_dev.modules.adminsaas.members.application.port.output.MemberRepositoryPort;
import resto_dev.shared.security.permissions.PermissionEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrganizationMemberRepositoryAdapter implements MemberRepositoryPort {

    private final OrganizationMemberJpaRepository jpaRepository;
    private final OrganizationMemberJpaMapper mapper;

    @Override
    public Optional<OrganizationMember> findByOrganizationAndUser(UUID organizationId, UUID userId) {
        return jpaRepository.findByOrganizationIdAndUserId(organizationId, userId).stream()
                .findFirst()
                .map(mapper::toDomain);
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
}
