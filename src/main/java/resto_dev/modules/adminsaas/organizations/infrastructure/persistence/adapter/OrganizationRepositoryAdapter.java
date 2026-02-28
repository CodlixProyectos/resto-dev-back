package resto_dev.modules.adminsaas.organizations.infrastructure.persistence.adapter;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.entity.OrganizationJpaEntity;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.repository.OrganizationJpaRepository;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.mapper.OrganizationJpaMapper;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import resto_dev.modules.adminsaas.organizations.domain.model.Organization;
import resto_dev.modules.adminsaas.organizations.application.port.output.OrganizationRepositoryPort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrganizationRepositoryAdapter implements OrganizationRepositoryPort {

    private final OrganizationJpaRepository jpaRepository;
    private final OrganizationJpaMapper mapper;

    @Override
    public Organization save(Organization organization) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(organization)));
    }

    @Override
    public Optional<Organization> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Organization> findBySlug(String slug) {
        return jpaRepository.findBySlug(slug).map(mapper::toDomain);
    }

    @Override
    public List<Organization> findByOwnerId(UUID ownerId) {
        return jpaRepository.findByOwnerId(ownerId).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsBySlug(String slug) {
        return jpaRepository.existsBySlug(slug);
    }
}
