package databse;

import java.sql.*;

public class UserJDBC {

    private static final String DB_URL = "jdbc:sqlite:events.db";

    // Initializes only the users table
    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "username TEXT PRIMARY KEY," +
                    "password TEXT NOT NULL)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Validates a user against the database
    public static boolean validateUser(String username, String encryptedPassword) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT password FROM users WHERE username = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("password").equals(encryptedPassword);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Updates password after verifying old password
    public static boolean updateUserPassword(String username, String oldEncryptedPassword, String newEncryptedPassword) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement check = conn.prepareStatement(
                    "SELECT password FROM users WHERE username = ?");
            check.setString(1, username);
            ResultSet rs = check.executeQuery();

            if (rs.next() && rs.getString("password").equals(oldEncryptedPassword)) {
                PreparedStatement update = conn.prepareStatement(
                        "UPDATE users SET password = ? WHERE username = ?");
                update.setString(1, newEncryptedPassword);
                update.setString(2, username);
                return update.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Inserts a new user (optional utility method)
    public static boolean insertUser(String username, String encryptedPassword) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (username, password) VALUES (?, ?)");
            ps.setString(1, username);
            ps.setString(2, encryptedPassword);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
            return false;
        }
    }
}
