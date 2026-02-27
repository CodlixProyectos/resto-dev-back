package resto_dev.modules.adminsaas.users.domain;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User domain entity — pure POJO, no JPA annotations.
 * Represents the core user concept in the domain layer.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private UUID id;
    private String email;
    private String passwordHash;
    private String fullName;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
