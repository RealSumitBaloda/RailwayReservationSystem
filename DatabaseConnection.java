import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * Manages JDBC Database Connection and Automatic Table Initialization
 * for PLATRA Railway Reservation System.
 */
public class DatabaseConnection {

    private static final String CONFIG_FILE = "db.properties";
    private static String url = "jdbc:mysql://localhost:3306/railway_reservation?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String user = "root";
    private static String password = "";

    private static Connection connection = null;

    static {
        loadProperties();
    }

    /**
     * Ensures MySQL JDBC Driver is properly registered.
     */
    public static void loadDriver() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e2) {
                throw new SQLException(
                    "MySQL JDBC Driver not found. Ensure mysql-connector-j.jar is in classpath.\n" +
                    "In IntelliJ IDEA: File -> Project Structure -> Modules -> Dependencies -> Add 'lib/mysql-connector-j-8.3.0.jar'.", e
                );
            }
        }
    }

    /**
     * Loads database credentials from db.properties file or classpath.
     */
    public static void loadProperties() {
        Properties props = new Properties();
        File file = new File(CONFIG_FILE);
        boolean loaded = false;

        if (file.exists()) {
            try (InputStream input = new FileInputStream(file)) {
                props.load(input);
                loaded = true;
            } catch (Exception e) {
                System.err.println("Could not read " + CONFIG_FILE + ": " + e.getMessage());
            }
        }

        if (!loaded) {
            try (InputStream in = DatabaseConnection.class.getResourceAsStream("/" + CONFIG_FILE)) {
                if (in != null) {
                    props.load(in);
                    loaded = true;
                }
            } catch (Exception ignored) {
            }
        }

        if (loaded) {
            url = props.getProperty("db.url", url);
            user = props.getProperty("db.user", user);
            password = props.getProperty("db.password", password);
        } else {
            saveProperties();
        }
    }

    /**
     * Saves current database properties to db.properties.
     */
    public static void saveProperties() {
        Properties props = new Properties();
        props.setProperty("db.url", url);
        props.setProperty("db.user", user);
        props.setProperty("db.password", password);
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "PLATRA Railway Reservation System - Database Config");
        } catch (Exception e) {
            System.err.println("Could not save db.properties: " + e.getMessage());
        }
    }

    public static void updateCredentials(String newUrl, String newUser, String newPassword) {
        url = newUrl;
        user = newUser;
        password = newPassword;
        saveProperties();
        closeConnection();
    }

    public static String getUrl() {
        return url;
    }

    public static String getUser() {
        return user;
    }

    public static String getPassword() {
        return password;
    }

    /**
     * Tests whether active MySQL connection can be established.
     */
    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Retrieves an active Connection instance.
     */
    public static Connection getConnection() throws SQLException {
        loadDriver();

        if (connection == null || connection.isClosed() || !isConnectionValid(connection)) {
            connection = DriverManager.getConnection(url, user, password);
        }
        return connection;
    }

    private static boolean isConnectionValid(Connection conn) {
        try {
            return conn != null && !conn.isClosed() && conn.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Closes the active connection if open.
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {
        } finally {
            connection = null;
        }
    }

    /**
     * Initializes tables and default records if they do not exist.
     */
    public static boolean initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Users table
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS users (" +
                    "  id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  username VARCHAR(50) NOT NULL UNIQUE," +
                    "  password VARCHAR(100) NOT NULL," +
                    "  full_name VARCHAR(100) DEFAULT 'System User'," +
                    "  role VARCHAR(20) DEFAULT 'admin'," +
                    "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")"
            );

            // Insert default admin
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT IGNORE INTO users (username, password, full_name, role) VALUES ('admin', '1234', 'PLATRA Administrator', 'admin')")) {
                pstmt.executeUpdate();
            }

            // 2. Trains table
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS trains (" +
                    "  train_number VARCHAR(10) PRIMARY KEY," +
                    "  train_name VARCHAR(100) NOT NULL," +
                    "  source VARCHAR(50) NOT NULL," +
                    "  destination VARCHAR(50) NOT NULL," +
                    "  departure_time VARCHAR(20) NOT NULL," +
                    "  arrival_time VARCHAR(20) NOT NULL," +
                    "  duration VARCHAR(20) NOT NULL," +
                    "  total_seats INT NOT NULL," +
                    "  available_seats INT NOT NULL," +
                    "  base_fare INT NOT NULL," +
                    "  status VARCHAR(50) DEFAULT 'On Time'" +
                    ")"
            );

            // Check if trains exist; if empty, insert seed trains
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM trains");
            if (rs.next() && rs.getInt(1) == 0) {
                seedInitialTrains(conn);
            }

            // 3. Bookings table
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS bookings (" +
                    "  pnr VARCHAR(20) PRIMARY KEY," +
                    "  train_number VARCHAR(10) NOT NULL," +
                    "  passenger_name VARCHAR(100) NOT NULL," +
                    "  seats INT NOT NULL," +
                    "  fare_class VARCHAR(20) NOT NULL," +
                    "  total_fare DOUBLE NOT NULL," +
                    "  booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "  FOREIGN KEY (train_number) REFERENCES trains(train_number) ON DELETE CASCADE" +
                    ")"
            );

            return true;
        } catch (Exception e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            return false;
        }
    }

    private static void seedInitialTrains(Connection conn) throws SQLException {
        String insertSql = "INSERT INTO trains (train_number, train_name, source, destination, departure_time, arrival_time, duration, total_seats, available_seats, base_fare, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            Object[][] initialTrains = {
                {"12951", "Mumbai Rajdhani", "Mumbai", "New Delhi", "17:00", "08:35", "15h 35m", 120, 48, 1450, "On Time"},
                {"12009", "Shatabdi Express", "Mumbai", "Ahmedabad", "06:25", "11:10", "4h 45m", 100, 32, 950, "On Time"},
                {"12957", "Swarna Jayanti Rajdhani", "Ahmedabad", "New Delhi", "18:20", "07:35", "13h 15m", 120, 65, 1350, "On Time"},
                {"12423", "Dibrugarh Rajdhani", "New Delhi", "Dibrugarh", "16:10", "07:00", "38h 50m", 150, 91, 2100, "Delayed 10 min"},
                {"12904", "Golden Temple Mail", "Mumbai", "Amritsar", "18:45", "22:30", "27h 45m", 160, 73, 1250, "On Time"},
                {"12431", "Rajdhani Express", "New Delhi", "Thiruvananthapuram", "11:00", "14:00", "51h 00m", 180, 110, 2400, "On Time"},
                {"12910", "Garib Rath", "Bengaluru", "New Delhi", "21:30", "18:45", "45h 15m", 180, 57, 1100, "On Time"},
                {"12015", "Ajmer Shatabdi", "New Delhi", "Ajmer", "06:05", "12:45", "6h 40m", 100, 41, 850, "On Time"},
                {"12926", "Paschim Express", "Amritsar", "Mumbai", "08:10", "13:45", "29h 35m", 150, 83, 1300, "On Time"},
                {"12860", "Gitanjali Express", "Mumbai", "Kolkata", "06:00", "10:00", "28h 00m", 160, 76, 1400, "On Time"}
            };

            for (Object[] t : initialTrains) {
                ps.setString(1, (String) t[0]);
                ps.setString(2, (String) t[1]);
                ps.setString(3, (String) t[2]);
                ps.setString(4, (String) t[3]);
                ps.setString(5, (String) t[4]);
                ps.setString(6, (String) t[5]);
                ps.setString(7, (String) t[6]);
                ps.setInt(8, (Integer) t[7]);
                ps.setInt(9, (Integer) t[8]);
                ps.setInt(10, (Integer) t[9]);
                ps.setString(11, (String) t[10]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
