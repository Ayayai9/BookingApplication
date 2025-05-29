package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Order {
    private static int counter = 1; // increments with each order

    private String orderId;
    private final String timestamp;
    private final ArrayList<CartItem> items;
    private final double totalPrice;
    private final String itemString; // flattened version for display

    // Used when creating a new order
    public Order(ArrayList<CartItem> cart) {
        this.orderId = String.format("%04d", counter++);
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.items = new ArrayList<>(cart);
        this.totalPrice = items.stream()
                .mapToDouble(i -> i.getEvent().getPrice() * i.getQuantity())
                .sum();
        this.itemString = buildItemSummary();
    }

    // Used when reading from file
    private Order(String orderId, String timestamp, double totalPrice, String itemString) {
        this.orderId = orderId;
        this.timestamp = timestamp;
        this.totalPrice = totalPrice;
        this.itemString = itemString;
        this.items = new ArrayList<>(); // not needed for display
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String toRecord() {
        StringBuilder sb = new StringBuilder();
        sb.append(orderId).append("|")
                .append(timestamp).append("|")
                .append(totalPrice).append("|");
        for (CartItem item : items) {
            sb.append(item.getEvent().getTitle())
                    .append(" x ")
                    .append(item.getQuantity()).append(";");
        }
        return sb.toString();
    }

    public static Order fromRecord(String line) {
        String[] parts = line.split("\\|");
        return new Order(
                parts[0],
                parts[1],
                Double.parseDouble(parts[2]),
                parts.length >= 4 ? parts[3] : ""
        );
    }

    public String getDisplay() {
        return String.format("Order #%s\nDate: %s\nItems: %s\nTotal: $%.2f\n",
                orderId, timestamp, itemString, totalPrice);
    }

    private String buildItemSummary() {
        StringBuilder sb = new StringBuilder();
        for (CartItem item : items) {
            sb.append(item.getEvent().getTitle()).append(" x ").append(item.getQuantity()).append("; ");
        }
        return sb.toString().trim();
    }

    public static void updateCounterFromFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int max = 0;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 1) {
                    try {
                        int id = Integer.parseInt(parts[0].replaceAll("[^\\d]", ""));
                        if (id > max) max = id;
                    } catch (NumberFormatException ignored) {}
                }
            }
            counter = max + 1;
        } catch (Exception e) {
            System.err.println("Error updating order counter: " + e.getMessage());
        }
    }
}
