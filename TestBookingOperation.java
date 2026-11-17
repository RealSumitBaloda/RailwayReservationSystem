import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Diagnostic test to verify Database Connection and Booking Operations.
 */
public class TestBookingOperation {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Starting Railway System DB & Booking Test...");
        System.out.println("==================================================");

        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("[1] Database Connection: SUCCESS");
            System.out.println("    Connected to Catalog: " + conn.getCatalog());
            System.out.println("    Driver: " + conn.getMetaData().getDriverName() + " (" + conn.getMetaData().getDriverVersion() + ")");

            // Step 1: Read Trains from MySQL
            System.out.println("\n[2] Testing Train Record Loading from MySQL...");
            int trainCount = 0;
            String testTrainNumber = null;
            int initialSeats = 0;

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT train_number, train_name, source, destination, available_seats FROM trains")) {
                while (rs.next()) {
                    if (trainCount == 0) {
                        testTrainNumber = rs.getString("train_number");
                        initialSeats = rs.getInt("available_seats");
                    }
                    trainCount++;
                }
            }
            System.out.println("    Successfully loaded " + trainCount + " trains from MySQL 'trains' table.");
            if (trainCount == 0 || testTrainNumber == null) {
                throw new IllegalStateException("No trains found in MySQL table 'trains'!");
            }
            System.out.println("    Using Train #" + testTrainNumber + " (Available seats before test: " + initialSeats + ")");

            // Step 2: Test Booking Operation in MySQL
            System.out.println("\n[3] Testing Booking Transaction...");
            String testPnr = "PNRTEST" + (System.currentTimeMillis() % 100000);
            int bookedSeats = 2;

            conn.setAutoCommit(false);
            try {
                // Insert booking
                String insertSql = "INSERT INTO bookings (pnr, train_number, passenger_name, seats, fare_class, total_fare) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setString(1, testPnr);
                    ps.setString(2, testTrainNumber);
                    ps.setString(3, "Automated Test Passenger");
                    ps.setInt(4, bookedSeats);
                    ps.setString(5, "3A");
                    ps.setDouble(6, 1950.0);
                    ps.executeUpdate();
                }

                // Decrement seats
                String updateSql = "UPDATE trains SET available_seats = available_seats - ? WHERE train_number = ?";
                try (PreparedStatement ps2 = conn.prepareStatement(updateSql)) {
                    ps2.setInt(1, bookedSeats);
                    ps2.setString(2, testTrainNumber);
                    ps2.executeUpdate();
                }

                conn.commit();
                System.out.println("    Booking inserted with PNR: " + testPnr);
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

            // Step 3: Verify Booking in MySQL
            System.out.println("\n[4] Verifying Booking in MySQL...");
            boolean verified = false;
            int updatedSeats = 0;
            String checkBooking = "SELECT b.pnr, b.passenger_name, b.seats, t.available_seats FROM bookings b JOIN trains t ON b.train_number = t.train_number WHERE b.pnr = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkBooking)) {
                ps.setString(1, testPnr);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        verified = true;
                        updatedSeats = rs.getInt("available_seats");
                        System.out.println("    Verified: PNR=" + rs.getString("pnr") + ", Passenger=" + rs.getString("passenger_name") + ", SeatsBooked=" + rs.getInt("seats"));
                        System.out.println("    Updated available seats in MySQL: " + updatedSeats + " (Expected: " + (initialSeats - bookedSeats) + ")");
                    }
                }
            }

            if (!verified || updatedSeats != (initialSeats - bookedSeats)) {
                throw new IllegalStateException("Booking verification failed!");
            }

            // Step 4: Test Cancellation & Cleanup
            System.out.println("\n[5] Testing Cancellation & Cleanup...");
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM bookings WHERE pnr = ?")) {
                    ps.setString(1, testPnr);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps2 = conn.prepareStatement("UPDATE trains SET available_seats = available_seats + ? WHERE train_number = ?")) {
                    ps2.setInt(1, bookedSeats);
                    ps2.setString(2, testTrainNumber);
                    ps2.executeUpdate();
                }
                conn.commit();
                System.out.println("    Test booking deleted and seats restored.");
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

            System.out.println("\n==================================================");
            System.out.println(" ALL TESTS PASSED: JDBC driver, connection, train records,");
            System.out.println(" and booking CRUD operations in MySQL are 100% operational!");
            System.out.println("==================================================");

        } catch (Exception e) {
            System.err.println("\n==================================================");
            System.err.println(" TEST FAILED: " + e.getMessage());
            e.printStackTrace();
            System.err.println("==================================================");
            System.exit(1);
        }
    }
}
