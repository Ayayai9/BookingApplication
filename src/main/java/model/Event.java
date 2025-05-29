package model;

public class Event {
    private String title;
    private String location;
    private String day;
    private double price;
    private int total;
    private int sold;
    private boolean enabled;

    public Event(String title, String location, String day, double price, int total, int sold, boolean enabled) {
        this.title = title;
        this.location = location;
        this.day = day;
        this.price = price;
        this.total = total;
        this.sold = sold;
        this.enabled = enabled;
    }

    public boolean isEnabled() { return enabled; }
    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public String getDay() { return day; }
    public double getPrice() { return price; }
    public int getTotal() { return total; }
    public int getSold() { return sold; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setSold(int sold) {
        this.sold = sold;
    }
    public void setTotal(int total) {
        this.total = total;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public void setDay(String day) {
        this.day = day;
    }
    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | $%.2f | %d/%d tickets | %s",
                title, location, day, price, sold, total, enabled ? "Enabled" : "Disabled");
    }

}
