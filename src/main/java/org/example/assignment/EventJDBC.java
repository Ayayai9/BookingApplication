package org.example.assignment;

import java.sql.*;
import java.util.ArrayList;

public class EventJDBC {

    private static final String DB_URL = "jdbc:sqlite:events.db";

    public static void initializeTable() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS events (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT,
                    location TEXT,
                    day TEXT,
                    price REAL,
                    sold INTEGER,
                    total INTEGER,
                    enabled BOOLEAN
                )
            """);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateEventByKey(Event original, Event updated) {
        String sql = """
        UPDATE events
        SET location = ?, day = ?, price = ?, total = ?, sold = ?, enabled = ?
        WHERE title = ? AND location = ? AND day = ?
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Updated values
            ps.setString(1, updated.getLocation());
            ps.setString(2, updated.getDay());
            ps.setDouble(3, updated.getPrice());
            ps.setInt(4, updated.getTotal());
            ps.setInt(5, updated.getSold());
            ps.setBoolean(6, updated.isEnabled());

            // WHERE clause (original values)
            ps.setString(7, original.getTitle());
            ps.setString(8, original.getLocation());
            ps.setString(9, original.getDay());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Event> getAllEvents() {
        ArrayList<Event> events = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM events")) {

            while (rs.next()) {
                events.add(new Event(
                        rs.getString("title"),
                        rs.getString("location"),
                        rs.getString("day"),
                        rs.getDouble("price"),
                        rs.getInt("total"),
                        rs.getInt("sold"),
                        rs.getBoolean("enabled")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    public static void insertEvent(Event e) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO events (title, location, day, price, sold, total, enabled)
                VALUES (?, ?, ?, ?, ?, ?, ?)
             """)) {
            ps.setString(1, e.getTitle());
            ps.setString(2, e.getLocation());
            ps.setString(3, e.getDay());
            ps.setDouble(4, e.getPrice());
            ps.setInt(5, e.getSold());
            ps.setInt(6, e.getTotal());
            ps.setBoolean(7, e.isEnabled());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void updateEvent(Event e) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement("""
                UPDATE events SET location = ?, day = ?, price = ?, sold = ?, total = ?, enabled = ?
                WHERE title = ? AND location = ? AND day = ?
             """)) {
            ps.setString(1, e.getLocation());
            ps.setString(2, e.getDay());
            ps.setDouble(3, e.getPrice());
            ps.setInt(4, e.getSold());
            ps.setInt(5, e.getTotal());
            ps.setBoolean(6, e.isEnabled());
            ps.setString(7, e.getTitle());
            ps.setString(8, e.getLocation());
            ps.setString(9, e.getDay());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void deleteByTitle(String title) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM events WHERE title = ?");
            ps.setString(1, title);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
