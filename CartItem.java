package org.example.assignment2;

public class CartItem {
    private Event event;
    private int quantity;

    public CartItem(Event event, int quantity) {
        this.event = event;
        this.quantity = quantity;
    }

    public Event getEvent() { return event; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
