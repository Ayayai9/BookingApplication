package controller;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Event;
import databse.EventJDBC;
import databse.OrderJDBC;

import java.util.*;

public class AdminDashboard extends Application {

    private final Map<String, String> eventOptionsMap = new LinkedHashMap<>();
    private ArrayList<Event> events;

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
            primaryStage.close();
            try {
                new LoginApp().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Button addBtn = new Button("Add Event");
        Button deleteBtn = new Button("Delete Event");
        Button modifyBtn = new Button("Modify Event");
        Button disableBtn = new Button("Disable Selected");
        Button enableBtn = new Button("Enable Selected");
        Button viewOrdersBtn = new Button("View All Orders");

        viewOrdersBtn.setOnAction(e -> {
            Stage orderStage = new Stage();
            orderStage.setTitle("All Orders (Admin)");

            ListView<String> orderList = new ListView<>();
            ArrayList<String> allOrders = OrderJDBC.getAllOrders();
            Collections.reverse(allOrders); // Most recent first
            orderList.getItems().addAll(allOrders);

            VBox layout = new VBox(10, new Label("All Orders:"), orderList);
            layout.setPadding(new Insets(15));
            Scene scene = new Scene(layout, 500, 400);
            orderStage.setScene(scene);
            orderStage.show();
        });


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

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(button -> {
                if (button == ButtonType.OK) {
                    try {
                        Event newEvent = new Event(
                                titleField.getText().trim(),
                                venueField.getText().trim(),
                                dayField.getText().trim(),
                                Double.parseDouble(priceField.getText()),
                                Integer.parseInt(capacityField.getText()),
                                0, true
                        );
                        EventJDBC.insertEvent(newEvent);
                        loadGroupedEvents();
                        table.getItems().setAll(eventOptionsMap.keySet());
                        return newEvent;
                    } catch (Exception ex) {
                        showAlert("Invalid input: " + ex.getMessage());
                    }
                }
                return null;
            });

            dialog.showAndWait();
        });

        deleteBtn.setOnAction(e -> {
            String selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                EventJDBC.deleteByTitle(selected);
                loadGroupedEvents();
                table.getItems().setAll(eventOptionsMap.keySet());
            }
        });

        modifyBtn.setOnAction(e -> {
            String selectedTitle = table.getSelectionModel().getSelectedItem();
            if (selectedTitle != null) {
                modifyEventDialog(selectedTitle, table); // pass table
            }
        });

        disableBtn.setOnAction(e -> toggleEventStatusDialog(table.getSelectionModel().getSelectedItem(), false, table));
        enableBtn.setOnAction(e -> toggleEventStatusDialog(table.getSelectionModel().getSelectedItem(), true, table));

        HBox buttonBox = new HBox(10, disableBtn, enableBtn, addBtn, deleteBtn, modifyBtn, viewOrdersBtn, logoutBtn);
        VBox root = new VBox(10, welcomeLabel, table, buttonBox);
        root.setPadding(new Insets(20));
        Scene scene = new Scene(root, 700, 450);

        primaryStage.setTitle("Admin Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadGroupedEvents() {
        events = EventJDBC.getAllEvents();
        eventOptionsMap.clear();

        Map<String, List<String>> eventMap = new LinkedHashMap<>();
        for (Event e : events) {
            String line = (e.isEnabled() ? "" : "[DISABLED] ") + e.getLocation() + " - " + e.getDay();
            eventMap.computeIfAbsent(e.getTitle(), k -> new ArrayList<>()).add(line);
        }

        eventMap.forEach((title, details) -> eventOptionsMap.put(title, String.join(", ", details)));
    }

    private void modifyEventDialog(String title, TableView<String> table) {
        // Step 1: Find all events with the selected title
        List<Event> matchedEvents = events.stream()
                .filter(e -> e.getTitle().equals(title))
                .toList();

        if (matchedEvents.isEmpty()) {
            showAlert("No events found with this title.");
            return;
        }

        // Step 2: Let user select which specific performance (same title, different venue/day)
        ChoiceDialog<Event> choiceDialog = new ChoiceDialog<>(matchedEvents.get(0), matchedEvents);
        choiceDialog.setTitle("Modify Event");
        choiceDialog.setHeaderText("Choose a specific event to modify:");
        choiceDialog.setContentText("Event:");

        Optional<Event> selected = choiceDialog.showAndWait();
        if (selected.isEmpty()) return;

        Event original = selected.get(); // preserve original to match WHERE clause
        Event editableCopy = new Event(
                original.getTitle(),
                original.getLocation(),
                original.getDay(),
                original.getPrice(),
                original.getTotal(),
                original.getSold(),
                original.isEnabled()
        );

        // Step 3: Dialog to edit the fields
        Dialog<Event> dialog = new Dialog<>();
        dialog.setTitle("Edit Event");

        TextField locationField = new TextField(editableCopy.getLocation());
        TextField dayField = new TextField(editableCopy.getDay());
        TextField priceField = new TextField(String.valueOf(editableCopy.getPrice()));
        TextField totalField = new TextField(String.valueOf(editableCopy.getTotal()));

        VBox content = new VBox(10,
                new Label("Venue:"), locationField,
                new Label("Day:"), dayField,
                new Label("Price:"), priceField,
                new Label("Total Tickets:"), totalField
        );
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    editableCopy.setLocation(locationField.getText().trim());
                    editableCopy.setDay(dayField.getText().trim());
                    editableCopy.setPrice(Double.parseDouble(priceField.getText().trim()));
                    editableCopy.setTotal(Integer.parseInt(totalField.getText().trim()));

                    // Step 4: Update via JDBC with original key
                    EventJDBC.updateEventByKey(original, editableCopy);
                    showAlert("Event updated successfully.");
                    return editableCopy;
                } catch (NumberFormatException ex) {
                    showAlert("Invalid input format.");
                }
            }
            return null;
        });

        dialog.showAndWait();
        loadGroupedEvents();
        table.getItems().setAll(eventOptionsMap.keySet());
    }


    private void toggleEventStatusDialog(String title, boolean enable, TableView<String> table) {
        List<Event> matchedEvents = events.stream().filter(e -> e.getTitle().equals(title)).toList();
        if (matchedEvents.isEmpty()) return;

        ChoiceDialog<Event> dialog = new ChoiceDialog<>(matchedEvents.get(0), matchedEvents);
        dialog.setTitle(enable ? "Enable Event" : "Disable Event");
        dialog.setHeaderText("Choose event to " + (enable ? "enable" : "disable"));

        dialog.showAndWait().ifPresent(e -> {
            e.setEnabled(enable);
            EventJDBC.updateEvent(e);
            loadGroupedEvents();
            table.getItems().setAll(eventOptionsMap.keySet());
        });
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
