package org.example.assignment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserMigration {

    private static final String DB_URL = "jdbc:sqlite:events.db";

    public static void migrateUsersFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename));
             Connection conn = DriverManager.getConnection(DB_URL)) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split(";");
                if (parts.length == 2) {
                    String username = parts[0];
                    String password = parts[1];

                    // Check if user already exists
                    PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?");
                    checkStmt.setString(1, username);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) {
                        // Insert only if user doesn't already exist
                        PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
                        insertStmt.setString(1, username);
                        insertStmt.setString(2, password);
                        insertStmt.executeUpdate();
                        insertStmt.close();
                    }
                    checkStmt.close();
                }
            }
            System.out.println("Migration completed.");
        } catch (Exception e) {
            System.err.println("Migration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

