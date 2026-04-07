package resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.output;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateSaaSOrganizationResponse {
    private String organizationId;
    private String name;
    private String slug;
    private String schemaName;
    private String temporaryPassword; // Envía a la pantalla la clave en plano, para que el Admin la copie
}
