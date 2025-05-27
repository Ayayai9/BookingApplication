package org.example.assignment2;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;

public class AdminDashboard extends Application {

    private final Map<String, String> eventOptionsMap = new LinkedHashMap<>();
    private final ArrayList<Event> events = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        Label welcomeLabel = new Label("Welcome, Admin!");

        TableView<String> table = new TableView<>();
        TableColumn<String, String> titleCol = new TableColumn<>("Event Title");
        titleCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue()));

        TableColumn<String, String> detailCol = new TableColumn<>("Options (Venue - Day)");

        detailCol.setCellValueFactory(data -> {
            String title = data.getValue();
            String details = eventOptionsMap.getOrDefault(title, "No Info");
            return new ReadOnlyStringWrapper(details);
        });

        table.getColumns().addAll(titleCol, detailCol);

        loadGroupedEvents();
        table.getItems().addAll(eventOptionsMap.keySet());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> {
            primaryStage.close(); // Close Admin dashboard
            try {
                new LoginApp().start(new Stage()); // Open login screen
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Button addBtn = new Button("Add Event");
        Button deleteBtn = new Button("Delete Event");
        Button modifyBtn = new Button("Modify Event");
        modifyBtn.setOnAction(e -> {
            String selectedTitle = table.getSelectionModel().getSelectedItem();
            if (selectedTitle != null) {
                modifyEventDialog(selectedTitle);
                refreshTable(table);
            }
        });

        Button disableBtn = new Button("Disable Selected");
        Button enableBtn = new Button("Enable Selected");

        disableBtn.setOnAction(e -> {
            String selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                toggleEventStatusDialog(selected, false, table);// Show dialog to disable a specific performance
            }
        });

        enableBtn.setOnAction(e -> {
            String selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                toggleEventStatusDialog(selected, true, table); // Show dialog to enable a specific performance
            }
        });

        Button viewOrdersBtn = new Button("View All Orders");
        viewOrdersBtn.setOnAction(e -> showAllOrdersDialog());


        addBtn.setOnAction(e -> {
            Dialog<Event> dialog = new Dialog<>();
            dialog.setTitle("Add Event");

            TextField titleField = new TextField();
            TextField venueField = new TextField();
            TextField dayField = new TextField();
            TextField priceField = new TextField();
            TextField capacityField = new TextField();

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));
            grid.add(new Label("Title:"), 0, 0); grid.add(titleField, 1, 0);
            grid.add(new Label("Venue:"), 0, 1); grid.add(venueField, 1, 1);
            grid.add(new Label("Day:"), 0, 2); grid.add(dayField, 1, 2);
            grid.add(new Label("Price:"), 0, 3); grid.add(priceField, 1, 3);
            grid.add(new Label("Capacity:"), 0, 4); grid.add(capacityField, 1, 4);
            dialog.getDialogPane().setContent(grid);

            ButtonType addType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addType, ButtonType.CANCEL);

            dialog.setResultConverter(dialogBtn -> {
                if (dialogBtn == addType) {
                    String title = titleField.getText().trim();
                    String venue = venueField.getText().trim();
                    String day = dayField.getText().trim();
                    double price = Double.parseDouble(priceField.getText());
                    int total = Integer.parseInt(capacityField.getText());

                    // check for duplicates
                    boolean duplicate = events.stream().anyMatch(event ->
                            event.getTitle().equalsIgnoreCase(title) &&
                                    event.getLocation().equalsIgnoreCase(venue) &&
                                    event.getDay().equalsIgnoreCase(day)
                    );
                    if (duplicate) {
                        showAlert("Duplicate event exists.");
                        return null;
                    }

                    return new Event(title, venue, day, price, total, 0, true);
                }
                return null;
            });

            deleteBtn.setOnAction(deleteEvent -> {
                String selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setHeaderText("Delete all performances of " + selected + "?");
                    confirm.setContentText("Bookings may exist. Proceed?");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            events.removeIf(ev -> ev.getTitle().equals(selected));
                            saveEventsToFile(events);
                            refreshTable(table);
                        }
                    });
                }
            });


            dialog.showAndWait().ifPresent(newEvent -> {
                events.add(newEvent);
                saveEventsToFile(events);
                refreshTable(table);
            });
        });


        HBox buttonBox = new HBox(10, disableBtn, enableBtn, addBtn, deleteBtn, modifyBtn, viewOrdersBtn, logoutBtn);
        VBox root = new VBox(10, welcomeLabel, table, buttonBox);
        root.setPadding(new Insets(20));
        Scene scene = new Scene(root, 700, 450);

        primaryStage.setTitle("Admin Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void rebuildEventOptionsMap() {
        eventOptionsMap.clear();
        Map<String, List<String>> eventMap = new LinkedHashMap<>();

        for (Event e : events) {
            String title = e.getTitle();
            String line = (e.isEnabled() ? "" : "[DISABLED] ") + e.getLocation() + " - " + e.getDay();
            eventMap.putIfAbsent(title, new ArrayList<>());
            eventMap.get(title).add(line);
        }

        for (Map.Entry<String, List<String>> entry : eventMap.entrySet()) {
            String title = entry.getKey();
            String options = String.join(", ", entry.getValue());
            eventOptionsMap.put(title, options);
        }
    }


    private void updateEventEnabledStatus(String title, boolean enabled) {
        for (Event e : events) {
            if (e.getTitle().equals(title)) {
                e.setEnabled(enabled);
            }
        }
        saveEventsToFile(events);
    }

    private void refreshTable(TableView<String> table) {
        table.getItems().clear();
        rebuildEventOptionsMap(); // <-- NEW!
        table.getItems().addAll(eventOptionsMap.keySet());
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadGroupedEvents() {
        events.clear();
        eventOptionsMap.clear();

        try (BufferedReader br = new BufferedReader(new FileReader("events.dat"))) {
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

                    Event event = new Event(title, location, day, price, total, sold, enabled);
                    events.add(event);
                }
            }

            rebuildEventOptionsMap();

        } catch (Exception e) {
            System.err.println("Error loading grouped events: " + e.getMessage());
        }
    }


    private void saveEventsToFile(ArrayList<Event> events) {
        try (PrintWriter writer = new PrintWriter("events.dat")) {
            for (Event e : events) {
                writer.printf("%s;%s;%s;%.2f;%d;%d;%b%n",
                        e.getTitle(), e.getLocation(), e.getDay(),
                        e.getPrice(), e.getSold(), e.getTotal(), e.isEnabled());
            }
        } catch (Exception ex) {
            System.err.println("Error writing events: " + ex.getMessage());
        }
    }

    private void modifyEventDialog(String title) {
        List<Event> matchedEvents = events.stream()
                .filter(e -> e.getTitle().equals(title))
                .toList();

        if (matchedEvents.isEmpty()) {
            showAlert("No events found with this title.");
            return;
        }

        ChoiceDialog<Event> eventChoiceDialog = new ChoiceDialog<>(matchedEvents.get(0), matchedEvents);
        eventChoiceDialog.setTitle("Select Event to Modify");
        eventChoiceDialog.setHeaderText("Choose a specific event to modify (title, venue, day):");
        eventChoiceDialog.setContentText("Event:");

        Optional<Event> result = eventChoiceDialog.showAndWait();
        if (result.isEmpty()) return;

        Event event = result.get();

        Dialog<Event> dialog = new Dialog<>();
        dialog.setTitle("Modify Event");

        Label locationLabel = new Label("Venue:");
        TextField locationField = new TextField(event.getLocation());

        Label dayLabel = new Label("Day:");
        TextField dayField = new TextField(event.getDay());

        Label priceLabel = new Label("Price:");
        TextField priceField = new TextField(String.valueOf(event.getPrice()));

        Label totalLabel = new Label("Total Tickets:");
        TextField totalField = new TextField(String.valueOf(event.getTotal()));

        VBox fields = new VBox(10, locationLabel, locationField, dayLabel, dayField, priceLabel, priceField, totalLabel, totalField);
        fields.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(fields);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    String newLocation = locationField.getText().trim();
                    String newDay = dayField.getText().trim();
                    double newPrice = Double.parseDouble(priceField.getText().trim());
                    int newTotal = Integer.parseInt(totalField.getText().trim());

                    // Check for duplicates (title + venue + day)
                    boolean duplicateExists = events.stream().anyMatch(e ->
                            e != event &&
                                    e.getTitle().equals(event.getTitle()) &&
                                    e.getLocation().equalsIgnoreCase(newLocation) &&
                                    e.getDay().equalsIgnoreCase(newDay)
                    );

                    if (duplicateExists) {
                        showAlert("Duplicate event with same title, venue, and day already exists.");
                        return null;
                    }

                    // Apply changes
                    event.setLocation(newLocation);
                    event.setDay(newDay);
                    event.setPrice(newPrice);
                    event.setTotal(newTotal);

                    return event;
                } catch (NumberFormatException ex) {
                    showAlert("Invalid number input.");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updated -> {
            saveEventsToFile(events);
            showAlert("Event updated successfully.");
        });
    }

    private void toggleEventStatusDialog(String title, boolean enable, TableView<String> table) {
        List<Event> matchedEvents = events.stream()
                .filter(e -> e.getTitle().equals(title))
                .toList();

        if (matchedEvents.isEmpty()) {
            showAlert("No events found with this title.");
            return;
        }

        ChoiceDialog<Event> dialog = new ChoiceDialog<>(matchedEvents.get(0), matchedEvents);
        dialog.setTitle(enable ? "Enable Event" : "Disable Event");
        dialog.setHeaderText("Choose a specific event to " + (enable ? "enable" : "disable") + ":");
        dialog.setContentText("Event:");

        dialog.showAndWait().ifPresent(event -> {
            event.setEnabled(enable);
            saveEventsToFile(events);
            refreshTable(table); // refresh main table
            showAlert("Event " + (enable ? "enabled" : "disabled") + " successfully.");
        });
    }

    private void showAllOrdersDialog() {
        Stage orderStage = new Stage();
        orderStage.setTitle("All Orders (Admin)");

        ListView<String> orderList = new ListView<>();
        ArrayList<String> allOrders = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader("orders.dat"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Order order = Order.fromRecord(line);
                allOrders.add(order.getDisplay());
            }
        } catch (Exception e) {
            showAlert("Failed to read orders.dat: " + e.getMessage());
            return;
        }

        Collections.reverse(allOrders); // Most recent first
        orderList.getItems().addAll(allOrders);

        VBox layout = new VBox(10, new Label("All Orders:"), orderList);
        layout.setPadding(new Insets(15));
        Scene scene = new Scene(layout, 500, 400);
        orderStage.setScene(scene);
        orderStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
