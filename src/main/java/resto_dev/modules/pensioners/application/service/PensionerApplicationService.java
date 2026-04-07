package resto_dev.modules.pensioners.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.pensioners.application.port.input.AddPensionerUseCase;
import resto_dev.modules.pensioners.application.port.input.UpdatePensionerUseCase;
import resto_dev.modules.pensioners.application.port.input.GetPensionersUseCase;
import resto_dev.modules.pensioners.application.port.input.BulkAddPensionersUseCase;
import resto_dev.modules.pensioners.application.port.output.PensionerRepositoryPort;
import resto_dev.modules.pensioners.domain.model.Pensioner;
import resto_dev.shared.errors.DuplicateResourceException;
import resto_dev.shared.errors.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PensionerApplicationService implements AddPensionerUseCase, GetPensionersUseCase, UpdatePensionerUseCase, BulkAddPensionersUseCase {

    private final PensionerRepositoryPort pensionerRepository;

    @Override
    @Transactional
    public Pensioner execute(UUID organizationId, UUID pensionerId, String fullName, String dni, String email, String phoneNumber, boolean active) {
        Pensioner pensioner = pensionerRepository.findById(pensionerId)
                .orElseThrow(() -> new ResourceNotFoundException("Pensionista", pensionerId));

        // Normalization
        String refinedFullName = (fullName != null) ? fullName.trim() : pensioner.getFullName();
        String refinedDni = (dni != null) ? dni.trim() : null;
        String refinedEmail = (email != null) ? email.trim().toLowerCase() : null;
        String refinedPhone = (phoneNumber != null) ? phoneNumber.trim() : null;

        // Duplicate Check (only if they changed)
        if (refinedDni != null && !refinedDni.equals(pensioner.getDni())) {
            if (pensionerRepository.existsByOrganizationIdAndDni(organizationId, refinedDni)) {
                throw new DuplicateResourceException("Pensionista", "DNI", refinedDni);
            }
        }

        if (refinedEmail != null && !refinedEmail.equals(pensioner.getEmail())) {
            if (pensionerRepository.existsByOrganizationIdAndEmail(organizationId, refinedEmail)) {
                throw new DuplicateResourceException("Pensionista", "Email", refinedEmail);
            }
        }

        pensioner.setFullName(refinedFullName);
        pensioner.setDni(refinedDni);
        pensioner.setEmail(refinedEmail);
        pensioner.setPhoneNumber(refinedPhone);
        pensioner.setActive(active);

        return pensionerRepository.save(pensioner);
    }

    @Override
    @Transactional
    public Pensioner execute(UUID organizationId, String fullName, String dni, String email, String phoneNumber) {
        // Normalization
        String refinedFullName = (fullName != null) ? fullName.trim() : null;
        String refinedDni = (dni != null) ? dni.trim() : null;
        String refinedEmail = (email != null) ? email.trim().toLowerCase() : null;
        String refinedPhone = (phoneNumber != null) ? phoneNumber.trim() : null;

        // Duplicate Check
        if (refinedDni != null && !refinedDni.isEmpty()) {
            if (pensionerRepository.existsByOrganizationIdAndDni(organizationId, refinedDni)) {
                throw new DuplicateResourceException("Pensionista", "DNI", refinedDni);
            }
        }

        if (refinedEmail != null && !refinedEmail.isEmpty()) {
            if (pensionerRepository.existsByOrganizationIdAndEmail(organizationId, refinedEmail)) {
                throw new DuplicateResourceException("Pensionista", "Email", refinedEmail);
            }
        }

        Pensioner pensioner = Pensioner.builder()
                .organizationId(organizationId)
                .fullName(refinedFullName)
                .dni(refinedDni)
                .email(refinedEmail)
                .phoneNumber(refinedPhone)
                .active(true)
                .build();
        
        return pensionerRepository.save(pensioner);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Pensioner> execute(UUID organizationId, Pageable pageable, String search) {
        return pensionerRepository.findAllByOrganizationId(organizationId, pageable, search);
    }

    @Override
    @Transactional
    public void execute(UUID organizationId, List<BulkPensionerRequest> pensioners) {
        for (BulkPensionerRequest req : pensioners) {
            // Normalization
            String refinedFullName = (req.fullName() != null) ? req.fullName().trim() : null;
            String refinedDni = (req.dni() != null) ? req.dni().trim() : null;
            String refinedEmail = (req.email() != null) ? req.email().trim().toLowerCase() : null;
            String refinedPhone = (req.phoneNumber() != null) ? req.phoneNumber().trim() : null;

            if (refinedFullName == null || refinedFullName.isEmpty()) continue;

            // Duplicate Check (Skip if exists)
            if (refinedDni != null && !refinedDni.isEmpty()) {
                if (pensionerRepository.existsByOrganizationIdAndDni(organizationId, refinedDni)) {
                    continue; // Skip existing DNI
                }
            }

            if (refinedEmail != null && !refinedEmail.isEmpty()) {
                if (pensionerRepository.existsByOrganizationIdAndEmail(organizationId, refinedEmail)) {
                    continue; // Skip existing Email
                }
            }

            Pensioner pensioner = Pensioner.builder()
                    .organizationId(organizationId)
                    .fullName(refinedFullName)
                    .dni(refinedDni)
                    .email(refinedEmail)
                    .phoneNumber(refinedPhone)
                    .active(true)
                    .build();
            
            pensionerRepository.save(pensioner);
        }
    }
}
