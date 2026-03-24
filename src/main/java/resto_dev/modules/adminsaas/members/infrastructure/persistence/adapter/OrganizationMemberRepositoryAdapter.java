package resto_dev.modules.adminsaas.members.infrastructure.persistence.adapter;

import resto_dev.modules.adminsaas.members.infrastructure.persistence.entity.OrganizationMemberJpaEntity;
import resto_dev.modules.adminsaas.members.infrastructure.persistence.repository.OrganizationMemberJpaRepository;
import resto_dev.modules.adminsaas.members.infrastructure.persistence.mapper.OrganizationMemberJpaMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import resto_dev.modules.adminsaas.members.domain.model.OrganizationMember;
import resto_dev.modules.adminsaas.members.application.port.output.MemberRepositoryPort;
import resto_dev.shared.security.permissions.PermissionEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import resto_dev.modules.adminsaas.members.domain.model.StaffStats;

@Component
@RequiredArgsConstructor
public class OrganizationMemberRepositoryAdapter implements MemberRepositoryPort {

    private final OrganizationMemberJpaRepository jpaRepository;
    private final OrganizationMemberJpaMapper mapper;

    @Override
    public OrganizationMember save(OrganizationMember member) {
        OrganizationMemberJpaEntity entity = mapper.toEntity(member);
        OrganizationMemberJpaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<OrganizationMember> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<OrganizationMember> findByOrganizationAndUser(UUID organizationId, UUID userId) {
        return jpaRepository.findByOrganizationIdAndUserId(organizationId, userId).stream()
                .findFirst()
                .map(mapper::toDomain);
    }

    @Override
    public Optional<OrganizationMember> findByOrganizationAndPin(UUID organizationId, String pin) {
        return jpaRepository.findByOrganizationIdAndPin(organizationId, pin).map(mapper::toDomain);
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

    @Override
    public StaffStats getStats(UUID organizationId) {
        long total = jpaRepository.countByOrganizationId(organizationId);
        long active = jpaRepository.countByOrganizationIdAndStatus(organizationId, "ACTIVE");
        long onLeave = jpaRepository.countByOrganizationIdAndStatus(organizationId, "ON_LEAVE");
        BigDecimal totalPayroll = jpaRepository.sumSalaryByOrganizationId(organizationId);

        return new StaffStats(
            total,
            active,
            onLeave,
            totalPayroll != null ? totalPayroll : BigDecimal.ZERO
        );
    }

    @Override
    public List<OrganizationMember> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<OrganizationMember> findAllByOrganization(UUID organizationId) {
        return jpaRepository.findByOrganizationId(organizationId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<OrganizationMember> findByEmailAndPin(String email, String pin) {
        return jpaRepository.findByUserEmailAndPin(email, pin)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<OrganizationMember> findByDniAndPin(String dni, String pin) {
        return jpaRepository.findByUserDniAndPin(dni, pin)
                .map(mapper::toDomain);
    }
}
