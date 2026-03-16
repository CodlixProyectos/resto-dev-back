package resto_dev.modules.adminsaas.pensioners.infrastructure.web.dto.input;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddPensionerRequest {
    @NotBlank(message = "El nombre completo es obligatorio")
    private String fullName;
    private String dni;
    private String email;
    private String phoneNumber;
}
