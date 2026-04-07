package resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.output;

import resto_dev.modules.adminsaas.members.infrastructure.web.dto.output.OrganizationMemberResponse;
import java.util.List;

public record SaasOrganizationDetailResponse(
    SaasOrganizationResponse organization,
    List<OrganizationMemberResponse> members
) {
}
