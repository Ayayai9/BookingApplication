package org.example.assignment;

import java.sql.*;
import java.util.ArrayList;

public class OrderJDBC {

    private static final String DB_URL = "jdbc:sqlite:events.db";

    public static void initializeOrdersTable() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                    "order_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT," +
                    "timestamp TEXT," +
                    "details TEXT," +
                    "total_price REAL" +
                    ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> getAllOrders() {
        ArrayList<String> orders = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM orders ORDER BY order_id DESC")) {

            while (rs.next()) {
                orders.add(rs.getString("details"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }


    public static void saveOrder(Order order, String username) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO orders (username, timestamp, details, total_price) VALUES (?, ?, ?, ?)");
            ps.setString(1, username);
            ps.setString(2, order.getTimestamp());
            ps.setString(3, order.getDisplay().replaceAll("\n", " | "));
            ps.setDouble(4, order.getTotalPrice());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> getOrderHistory(String username) {
        ArrayList<String> history = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT timestamp, details, total_price FROM orders WHERE username = ? ORDER BY order_id DESC");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String record = String.format("Order placed on %s\n%s\nTotal: $%.2f",
                        rs.getString("timestamp"),
                        rs.getString("details").replaceAll(" \\| ", "\n"),
                        rs.getDouble("total_price"));
                history.add(record);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
}
