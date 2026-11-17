import java.sql.Connection;

/**
 * Run this class in IntelliJ to verify MySQL connection instantly.
 */
public class TestConnection {
    public static void main(String[] args) {
        System.out.println("Testing MySQL connection...");
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("==================================================");
                System.out.println(" SUCCESS: Connected to MySQL Database!");
                System.out.println(" Database Name : " + conn.getCatalog());
                System.out.println(" MySQL Version : " + conn.getMetaData().getDatabaseProductVersion());
                System.out.println(" Driver        : " + conn.getMetaData().getDriverName());
                System.out.println("==================================================");
            }
        } catch (Exception e) {
            System.err.println("==================================================");
            System.err.println(" CONNECTION FAILED:");
            System.err.println(" " + e.getMessage());
            System.err.println(" Check db.properties to ensure your password is correct.");
            System.err.println("==================================================");
        }
    }
}
