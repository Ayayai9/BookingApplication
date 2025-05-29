package controller;

import databse.EventJDBC;
import databse.OrderJDBC;
import databse.UserJDBC;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.CartItem;
import model.Event;
import model.Order;
import util.PasswordUtils;

import java.io.PrintWriter;
import java.util.ArrayList;

public class EventTableViewer extends Application {

    private static String username;
    private final ArrayList<CartItem> cart = new ArrayList<>();
    private final TableView<Event> table = new TableView<>();

    public static void setUsername(String user) {
        username = user;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ArrayList<Event> events = EventJDBC.getAllEvents();
        events.removeIf(event -> !event.isEnabled());

        Label welcomeLabel = new Label("Welcome, " + username + "!");

        Button viewCartButton = new Button("View Cart");
        viewCartButton.setOnAction(e -> showCartWindow());

        Button viewOrdersButton = new Button("View Orders");
        viewOrdersButton.setOnAction(e -> showOrderHistory());

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            primaryStage.close();
            try {
                new LoginApp().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Button changePasswordButton = new Button("Change Password");
        changePasswordButton.setOnAction(e -> showChangePasswordDialog());

        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> System.exit(0));

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Event, String> titleCol = new TableColumn<>("EVENT");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Event, String> locationCol = new TableColumn<>("VENUE");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));

        TableColumn<Event, String> dayCol = new TableColumn<>("DAY");
        dayCol.setCellValueFactory(new PropertyValueFactory<>("day"));

        TableColumn<Event, Double> priceCol = new TableColumn<>("$PRICE(AUD)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Event, Integer> soldCol = new TableColumn<>("#SOLD TICKETS");
        soldCol.setCellValueFactory(new PropertyValueFactory<>("sold"));

        TableColumn<Event, Integer> totalCol = new TableColumn<>("#TOTAL TICKETS");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));

        table.getColumns().addAll(titleCol, locationCol, dayCol, priceCol, soldCol, totalCol);
        table.getItems().addAll(events);

        TableColumn<Event, Void> bookCol = new TableColumn<>("ACTION");
        bookCol.setCellFactory(getBookButtonCellFactory(table));
        table.getColumns().add(bookCol);

        HBox topButtons = new HBox(10, viewCartButton, viewOrdersButton);
        HBox bottomButtons = new HBox(10, logoutButton, changePasswordButton, exitButton);

        VBox vbox = new VBox(10, welcomeLabel, table, topButtons, bottomButtons);
        vbox.setPadding(new Insets(20));
        Scene scene = new Scene(vbox, 800, 400);

        primaryStage.setTitle("Dashboard - " + username);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void exportOrders() {
        String filename;
        switch (username) {
            case "user1": filename = "orders1.dat"; break;
            case "user2": filename = "orders2.dat"; break;
            case "user3": filename = "orders3.dat"; break;
            default: filename = "orders_export.dat";
        }

        ArrayList<String> lines = OrderJDBC.getOrderHistory(username);
        if (lines.isEmpty()) {
            showAlert("No orders found to export.");
            return;
        }

        // Reverse for most recent first
        java.util.Collections.reverse(lines);

        try (PrintWriter writer = new PrintWriter(filename)) {
            for (String line : lines) {
                writer.println(line);
                writer.println("-----------------------------");
            }
            showAlert("Orders exported to: " + filename);
        } catch (Exception e) {
            showAlert("Error writing to " + filename + ": " + e.getMessage());
        }
    }



    private void showOrderHistory() {
        Stage orderStage = new Stage();
        orderStage.setTitle("Order History");

        ListView<String> listView = new ListView<>();
        ArrayList<String> lines = OrderJDBC.getOrderHistory(username);
        listView.getItems().addAll(lines);

        VBox layout = new VBox(10, new Label("Your Orders:"), listView);
        layout.setPadding(new Insets(15));
        Scene scene = new Scene(layout, 500, 400);
        orderStage.setScene(scene);
        orderStage.show();
    }



    private Callback<TableColumn<Event, Void>, TableCell<Event, Void>> getBookButtonCellFactory(TableView<Event> table) {
        return param -> new TableCell<>() {
            private final Button bookBtn = new Button("Book");

            {
                bookBtn.setOnAction(event -> {
                    Event selectedEvent = getTableView().getItems().get(getIndex());
                    showBookingDialog(selectedEvent);
                });
            }

            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(bookBtn);
                }
            }
        };
    }

    private void showBookingDialog(Event event) {
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Book Tickets");
        dialog.setHeaderText("Booking for: " + event.getTitle());
        dialog.setContentText("Enter number of tickets:");

        dialog.showAndWait().ifPresent(input -> {
            try {
                int quantity = Integer.parseInt(input);
                if (quantity <= 0) {
                    showAlert("Please enter a positive number.");
                    return;
                }

                int available = event.getTotal();
                int alreadyInCart = cart.stream()
                        .filter(item -> item.getEvent().equals(event))
                        .mapToInt(CartItem::getQuantity)
                        .sum();

                if (quantity + alreadyInCart > available) {
                    showAlert("Not enough tickets. Only " + (available - alreadyInCart) + " remaining.");
                } else {
                    addToCart(event, quantity);
                    showAlert(quantity + " ticket(s) added to cart.");
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid number entered.");
            }
        });
    }

    private void addToCart(Event event, int quantity) {
        for (CartItem item : cart) {
            if (item.getEvent().equals(event)) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        cart.add(new CartItem(event, quantity));
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void showCartWindow() {
        Stage cartStage = new Stage();
        cartStage.setTitle("Your Cart");

        TableView<CartItem> cartTable = new TableView<>();
        cartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<CartItem, String> titleCol = new TableColumn<>("Event");
        titleCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getEvent().getTitle()));

        TableColumn<CartItem, String> venueCol = new TableColumn<>("Venue");
        venueCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getEvent().getLocation()));

        TableColumn<CartItem, String> dayCol = new TableColumn<>("Day");
        dayCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getEvent().getDay()));

        TableColumn<CartItem, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        cartTable.getColumns().addAll(titleCol, venueCol, dayCol, qtyCol);
        cartTable.getItems().addAll(cart);

        Label totalLabel = new Label();
        updateCartTotalLabel(totalLabel);

        Button checkoutButton = new Button("Checkout");
        checkoutButton.setOnAction(e -> {
            if (processCheckout()) {
                cart.clear();
                cartTable.getItems().clear();
                showAlert("Checkout successful!");
                cartStage.close();
            }
        });

        Button removeSelected = new Button("Remove Selected");
        removeSelected.setOnAction(e -> {
            CartItem selected = cartTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                cart.remove(selected);
                cartTable.getItems().remove(selected);
            }
        });

        Button modifyCartButton = new Button("Modify Cart");
        modifyCartButton.setOnAction(e -> cartStage.close()); // just closes and lets them go back

        HBox actionButtons = new HBox(10, removeSelected, checkoutButton, modifyCartButton);

        VBox layout = new VBox(10, cartTable, actionButtons, totalLabel);
        layout.setPadding(new Insets(15));
        Scene scene = new Scene(layout, 450, 320);
        cartStage.setScene(scene);
        cartStage.show();
    }
    private void updateCartTotalLabel(Label label) {
        double total = cart.stream()
                .mapToDouble(item -> item.getEvent().getPrice() * item.getQuantity())
                .sum();
        label.setText("Total Price: $" + String.format("%.2f", total));
    }

    private boolean processCheckout() {

        // 1. Validate event days (today or later)
        ArrayList<String> allowedDays = getAllowedDays();
        for (CartItem item : cart) {
            String eventDay = item.getEvent().getDay().trim();
            if (!allowedDays.contains(eventDay)) {
                showAlert("Checkout failed. You cannot book " + eventDay + " events today.");
                return false;
            }
        }

        // 2. Validate ticket availability
        for (CartItem item : cart) {
            Event event = item.getEvent();
            int total = event.getTotal();
            int qty = item.getQuantity();
            if (qty > total) {
                showAlert("Checkout failed. Not enough tickets for " + event.getTitle());
                return false;
            }
        }

        // 3. Ask user to enter 6-digit confirmation code
        TextInputDialog codeDialog = new TextInputDialog();
        codeDialog.setTitle("Confirm Payment");
        codeDialog.setHeaderText("Enter your 6-digit confirmation code");
        codeDialog.setContentText("Code:");
        String code = codeDialog.showAndWait().orElse("");

        if (!code.matches("\\d{6}")) {
            showAlert("Invalid confirmation code. It must be exactly 6 digits.");
            return false;
        }

        // 4. Calculate total price and confirm
        double totalPrice = cart.stream()
                .mapToDouble(item -> item.getEvent().getPrice() * item.getQuantity())
                .sum();

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Checkout");
        confirmAlert.setHeaderText("Total price: $" + totalPrice);
        confirmAlert.setContentText("Proceed to payment?");
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return false;
        }

        // Update sold counts
        for (CartItem item : cart) {
            Event event = item.getEvent();
            event.setSold(event.getSold() + item.getQuantity());
            event.setTotal(event.getTotal() - item.getQuantity());
        }

        // Save to file
        for (Event event : table.getItems()) {
            EventJDBC.updateEvent(event);
        }

        // Refresh table
        table.getItems().clear();
        table.getItems().setAll(EventJDBC.getAllEvents().stream()
                .filter(Event::isEnabled)
                .toList());

        Order order = new Order(cart);
        saveOrder(order);

        return true;
    }

    private void saveOrder(Order order) {
        OrderJDBC.saveOrder(order, username);
    }



    private void showChangePasswordDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Change Password");

        PasswordField oldPass = new PasswordField();
        oldPass.setPromptText("Old Password");

        PasswordField newPass = new PasswordField();
        newPass.setPromptText("New Password");

        VBox box = new VBox(10, new Label("Old Password:"), oldPass,
                new Label("New Password:"), newPass);
        box.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(box);

        ButtonType changeBtn = new ButtonType("Change", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeBtn, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == changeBtn) return newPass.getText();
            return null;
        });

        dialog.showAndWait().ifPresent(newPassword -> {
            if (updateUserPassword(username, oldPass.getText(), newPassword)) {
                showAlert("Password changed successfully.");
            } else {
                showAlert("Old password incorrect.");
            }
        });
    }

    private boolean updateUserPassword(String user, String oldPass, String newPass) {
        String encryptedOld = PasswordUtils.encrypt(oldPass);
        String encryptedNew = PasswordUtils.encrypt(newPass);
        return UserJDBC.updateUserPassword(user, encryptedOld, encryptedNew);
    }

    private ArrayList<String> getAllowedDays() {
        ArrayList<String> week = new ArrayList<>();
        week.add("Mon");
        week.add("Tue");
        week.add("Wed");
        week.add("Thu");
        week.add("Fri");
        week.add("Sat");
        week.add("Sun");


        String today = "Mon"; //for testing purposes
//        String today = java.time.LocalDate.now().getDayOfWeek().toString().substring(0, 3);
        today = today.substring(0, 1).toUpperCase() + today.substring(1).toLowerCase(); // e.g. "Wed"

        int start = week.indexOf(today);
        return new ArrayList<>(week.subList(start, week.size()));
    }


    public static void main(String[] args) {
        launch(args);
    }
}
