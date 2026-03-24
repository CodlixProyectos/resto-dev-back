package resto_dev.modules.adminsaas.members.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.adminsaas.members.application.port.input.GetMyPermissionsUseCase;
import resto_dev.modules.adminsaas.members.application.port.output.MemberRepositoryPort;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyPermissionsApplicationService implements GetMyPermissionsUseCase {

    private final MemberRepositoryPort memberRepository;

    @Override
    public List<String> execute(UUID organizationId, UUID userId) {
        return memberRepository.findPermissionsByOrganizationAndUser(organizationId, userId);
    }
}
