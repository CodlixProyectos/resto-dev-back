package resto_dev.modules.menu.publicmenu.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicOrganizationDTO {
    private UUID id;
    private String name;
    private String logoUrl;
    private String primaryColor;
    private String secondaryColor;
}
