import java.sql.Connection;
import java.sql.SQLException;

/**
 * DBConnection provides standard database connection access.
 * Serves as the primary/companion database utility for the project.
 */
public class DBConnection {

    public static Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public static void closeConnection() {
        DatabaseConnection.closeConnection();
    }

    public static boolean initializeDatabase() {
        return DatabaseConnection.initializeDatabase();
    }

    public static boolean testConnection() {
        return DatabaseConnection.testConnection();
    }
}
