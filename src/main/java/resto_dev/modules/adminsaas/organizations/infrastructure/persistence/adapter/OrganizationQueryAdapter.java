package resto_dev.modules.adminsaas.organizations.infrastructure.persistence.adapter;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import resto_dev.modules.adminsaas.organizations.application.port.output.OrganizationQueryPort;
import resto_dev.modules.adminsaas.organizations.application.query.GetOrganizationsQuery;
import resto_dev.modules.adminsaas.organizations.domain.model.Organization;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.entity.OrganizationJpaEntity;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.mapper.OrganizationJpaMapper;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.repository.OrganizationJpaRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter that connects the OrganizationQueryPort to the Spring Data JPA layer,
 * generating dynamic SQL based on criteria using Specifications.
 */
@Component
@RequiredArgsConstructor
public class OrganizationQueryAdapter implements OrganizationQueryPort {

    private final OrganizationJpaRepository repository;
    private final OrganizationJpaMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<Organization> searchOrganizations(GetOrganizationsQuery query) {
        // Page in UI is 1-based, Spring Data is 0-based
        int pageIndex = Math.max(0, query.page() - 1);
        int pageSize = query.size() > 0 ? query.size() : 10;

        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<OrganizationJpaEntity> spec = createSpecification(query);

        return repository.findAll(spec, pageable).map(mapper::toDomain);
    }

    private Specification<OrganizationJpaEntity> createSpecification(GetOrganizationsQuery query) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search filter (name or slug)
            if (query.search() != null && !query.search().trim().isEmpty()) {
                String likePattern = "%" + query.search().toLowerCase().trim() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), likePattern),
                        cb.like(cb.lower(root.get("slug")), likePattern),
                        cb.like(cb.lower(root.get("type")), likePattern)));
            }

            // isActive filter
            if (query.isActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), query.isActive()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
