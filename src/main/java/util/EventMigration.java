package util;

import databse.EventJDBC;
import model.Event;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;

public class EventMigration {

    public static void main(String[] args) {
        EventMigration("events.dat");
        ArrayList<Event> events = EventJDBC.getAllEvents();
        events.forEach(e -> System.out.println(e.getTitle() + " at " + e.getLocation()));
    }

    public static void EventMigration(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename));
             Connection conn = DriverManager.getConnection("jdbc:sqlite:events.db")) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(";");
                if (parts.length >= 6) {
                    String title = parts[0];
                    String location = parts[1];
                    String day = parts[2];
                    double price = Double.parseDouble(parts[3]);
                    int sold = Integer.parseInt(parts[4]);
                    int total = Integer.parseInt(parts[5]);
                    boolean enabled = parts.length == 7 ? Boolean.parseBoolean(parts[6]) : true;

                    PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO events (title, location, day, price, sold, total, enabled) VALUES (?, ?, ?, ?, ?, ?, ?)");
                    ps.setString(1, title);
                    ps.setString(2, location);
                    ps.setString(3, day);
                    ps.setDouble(4, price);
                    ps.setInt(5, sold);
                    ps.setInt(6, total);
                    ps.setBoolean(7, enabled);
                    ps.executeUpdate();
                }
            }
            System.out.println("Import complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
