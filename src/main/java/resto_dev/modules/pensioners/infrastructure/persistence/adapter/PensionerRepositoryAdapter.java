package resto_dev.modules.pensioners.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import resto_dev.modules.pensioners.application.port.output.PensionerRepositoryPort;
import resto_dev.modules.pensioners.domain.model.Pensioner;
import resto_dev.modules.pensioners.infrastructure.persistence.entity.PensionerJpaEntity;
import resto_dev.modules.pensioners.infrastructure.persistence.mapper.PensionerJpaMapper;
import resto_dev.modules.pensioners.infrastructure.persistence.repository.PensionerJpaRepository;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PensionerRepositoryAdapter implements PensionerRepositoryPort {

    private final PensionerJpaRepository jpaRepository;
    private final PensionerJpaMapper mapper;

    @Override
    public Pensioner save(Pensioner pensioner) {
        PensionerJpaEntity entity = mapper.toEntity(pensioner);
        PensionerJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Pensioner> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Pensioner> findAllByOrganizationId(UUID organizationId, Pageable pageable, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return jpaRepository.findAllByOrganizationIdAndSearch(organizationId, search, pageable)
                    .map(mapper::toDomain);
        }
        return jpaRepository.findAllByOrganizationId(organizationId, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByOrganizationIdAndDni(UUID organizationId, String dni) {
        return jpaRepository.existsByOrganizationIdAndDni(organizationId, dni);
    }

    @Override
    public boolean existsByOrganizationIdAndEmail(UUID organizationId, String email) {
        return jpaRepository.existsByOrganizationIdAndEmail(organizationId, email);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
