package resto_dev.modules.adminsaas.members.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.adminsaas.members.ports.in.GetMyPermissionsPort;
import resto_dev.modules.adminsaas.members.ports.out.MemberRepositoryPort;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyPermissionsUseCase implements GetMyPermissionsPort {

    private final MemberRepositoryPort memberRepository;

    @Override
    public List<String> execute(UUID organizationId, UUID userId) {
        return memberRepository.findPermissionsByOrganizationAndUser(organizationId, userId);
    }
}
