import java.sql.*;

public class CheckDB {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/resto_dev";
        String user = "postgres";
        String password = "123456789";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected to DB!");
            
            // 1. Check schemas
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT schema_name FROM information_schema.schemata");
                System.out.println("Schemas found:");
                while (rs.next()) {
                    System.out.println(" - " + rs.getString(1));
                }
            }

            // 2. Check organizations
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT id, name, schema_name FROM admin.organizations");
                System.out.println("Organizations in admin.organizations:");
                while (rs.next()) {
                    System.out.println(" - " + rs.getString(1) + " | " + rs.getString(2) + " | " + rs.getString(3));
                }
            }

            // 3. Check data in codlix schema
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT count(*) FROM codlix.category");
                if (rs.next()) {
                    System.out.println("Categories in 'codlix' schema: " + rs.getInt(1));
                }
            } catch (Exception e) {
                System.out.println("Error checking codlix schema: " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
