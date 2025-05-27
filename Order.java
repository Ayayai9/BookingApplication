package org.example.assignment2;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Order {
    private static int counter = 1; // increments with each order

    private final String orderId;
    private final String timestamp;
    private final ArrayList<CartItem> items;
    private final double totalPrice;

    public Order(ArrayList<CartItem> cart) {
        this.orderId = String.format("%04d", counter++);
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.items = new ArrayList<>(cart);
        this.totalPrice = items.stream()
                .mapToDouble(i -> i.getEvent().getPrice() * i.getQuantity())
                .sum();
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
        Order order = new Order(new ArrayList<>());
        order.orderIdOverride = parts[0];
        order.timestampOverride = parts[1];
        order.totalPriceOverride = Double.parseDouble(parts[2]);
        order.itemString = parts[3];
        return order;
    }

    // For reading only
    private String orderIdOverride;
    private String timestampOverride;
    private double totalPriceOverride;
    private String itemString;

    public String getDisplay() {
        return String.format("Order #%s\nDate: %s\nItems: %s\nTotal: $%.2f\n",
                orderIdOverride != null ? orderIdOverride : orderId,
                timestampOverride != null ? timestampOverride : timestamp,
                itemString != null ? itemString : buildItemSummary(),
                totalPriceOverride != 0 ? totalPriceOverride : totalPrice);
    }

    private String buildItemSummary() {
        StringBuilder sb = new StringBuilder();
        for (CartItem item : items) {
            sb.append(item.getEvent().getTitle()).append(" x ").append(item.getQuantity()).append("; ");
        }
        return sb.toString();
    }
}
