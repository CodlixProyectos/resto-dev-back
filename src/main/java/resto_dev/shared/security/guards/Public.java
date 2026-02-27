package resto_dev.shared.security.guards;

import java.lang.annotation.*;

/**
 * Marker annotation for endpoints that do NOT require authentication.
 * Used by SecurityConfig to whitelist public routes.
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Public {
}
