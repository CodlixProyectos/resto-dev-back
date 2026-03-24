package resto_dev.modules.adminsaas.members.infrastructure.web.dto.input;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PinLoginRequest {
    private String email;
    private String dni;

    @NotBlank(message = "PIN is required")
    private String pin;
}
