package resto_dev.modules.adminsaas.pensioners.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.adminsaas.pensioners.application.port.input.AddPensionerUseCase;
import resto_dev.modules.adminsaas.pensioners.application.port.input.GetPensionersUseCase;
import resto_dev.modules.adminsaas.pensioners.application.port.output.PensionerRepositoryPort;
import resto_dev.modules.adminsaas.pensioners.domain.model.Pensioner;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PensionerApplicationService implements AddPensionerUseCase, GetPensionersUseCase {

    private final PensionerRepositoryPort pensionerRepository;

    @Override
    @Transactional
    public Pensioner execute(UUID organizationId, String fullName, String dni, String email, String phoneNumber) {
        Pensioner pensioner = Pensioner.builder()
                .organizationId(organizationId)
                .fullName(fullName)
                .dni(dni)
                .email(email)
                .phoneNumber(phoneNumber)
                .active(true)
                .build();
        
        return pensionerRepository.save(pensioner);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Pensioner> execute(UUID organizationId, Pageable pageable) {
        return pensionerRepository.findAllByOrganizationId(organizationId, pageable);
    }
}
