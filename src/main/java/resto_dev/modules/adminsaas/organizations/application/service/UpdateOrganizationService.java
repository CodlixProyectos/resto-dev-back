package resto_dev.modules.adminsaas.organizations.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.adminsaas.organizations.application.port.input.UpdateOrganizationUseCase;
import resto_dev.modules.adminsaas.organizations.application.port.output.OrganizationRepositoryPort;
import resto_dev.modules.adminsaas.organizations.domain.model.Organization;
import resto_dev.shared.errors.ResourceNotFoundException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateOrganizationService implements UpdateOrganizationUseCase {

    private final OrganizationRepositoryPort organizationRepository;

    @Override
    @Transactional
    public Organization execute(UUID id, UpdateOrganizationCommand command) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        org.setName(command.name());
        org.setLegalName(command.legalName());
        org.setBusinessId(command.businessId());
        org.setEmail(command.email());
        org.setPhone(command.phone());
        org.setAddress(command.address());
        org.setLogoUrl(command.logoUrl());
        org.setSunatUser(command.sunatUser());
        org.setSunatPassword(command.sunatPassword());
        org.setSunatClientId(command.sunatClientId());
        org.setSunatClientSecret(command.sunatClientSecret());

        return organizationRepository.save(org);
    }
}
