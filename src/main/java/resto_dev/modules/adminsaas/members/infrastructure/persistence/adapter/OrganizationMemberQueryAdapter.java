package resto_dev.modules.adminsaas.members.infrastructure.persistence.adapter;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.adminsaas.members.application.port.output.OrganizationMemberQueryPort;
import resto_dev.modules.adminsaas.members.application.query.GetOrganizationMembersQuery;
import resto_dev.modules.adminsaas.members.domain.model.OrganizationMember;
import resto_dev.modules.adminsaas.members.infrastructure.persistence.entity.OrganizationMemberJpaEntity;
import resto_dev.modules.adminsaas.members.infrastructure.persistence.mapper.OrganizationMemberJpaMapper;
import resto_dev.modules.adminsaas.members.infrastructure.persistence.repository.OrganizationMemberJpaRepository;
import resto_dev.shared.security.permissions.RoleEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter that connects the OrganizationMemberQueryPort to the Spring Data JPA
 * layer,
 * generating dynamic SQL based on criteria using Specifications.
 */
@Component
@RequiredArgsConstructor
public class OrganizationMemberQueryAdapter implements OrganizationMemberQueryPort {

    private final OrganizationMemberJpaRepository repository;
    private final OrganizationMemberJpaMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<OrganizationMember> searchMembers(GetOrganizationMembersQuery query) {
        // Page in UI is 1-based, Spring Data is 0-based
        int pageIndex = Math.max(0, query.page() - 1);
        int pageSize = query.size() > 0 ? query.size() : 10;

        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<OrganizationMemberJpaEntity> spec = createSpecification(query);

        return repository.findAll(spec, pageable).map(mapper::toDomain);
    }

    private Specification<OrganizationMemberJpaEntity> createSpecification(GetOrganizationMembersQuery query) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by the current organization
            predicates.add(cb.equal(root.get("organization").get("id"), query.organizationId()));

            // Search filter by joining to user profile (Not easily doable without User
            // entity joined,
            // but we can search by Role Name as a fallback for the example,
            // since members table relies on User ID link and RoleEntity link).
            if (query.search() != null && !query.search().trim().isEmpty()) {
                String likePattern = "%" + query.search().toLowerCase().trim() + "%";
                Join<OrganizationMemberJpaEntity, RoleEntity> roleJoin = root.join("role");

                predicates.add(
                        cb.like(cb.lower(roleJoin.get("name")), likePattern));
            }

            // isActive filter
            if (query.isActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), query.isActive()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
