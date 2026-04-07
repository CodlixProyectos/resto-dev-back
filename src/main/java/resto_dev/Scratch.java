import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Scratch {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("password123 matches: " + encoder.matches("password123", "$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xdM0ttR9UJD4kK4G"));
        System.out.println("12345678 matches: " + encoder.matches("12345678", "$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xdM0ttR9UJD4kK4G"));
        System.out.println("admin matches: " + encoder.matches("admin", "$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xdM0ttR9UJD4kK4G"));
        System.out.println("admin123 matches: " + encoder.matches("admin123", "$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xdM0ttR9UJD4kK4G"));
        
        System.out.println("New hash for password123: " + encoder.encode("password123"));
    }
}
