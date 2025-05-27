package org.example.assignment;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        OrderJDBC.initializeOrdersTable();
        EventJDBC.initializeTable();
        UserJDBC.initializeDatabase(); // Ensure DB is set up

        Label userLabel = new Label("Username:");
        TextField username = new TextField();
        Label passLabel = new Label("Password:");
        PasswordField password = new PasswordField();
        Button loginButton = new Button("Login");
        Label status = new Label();

        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> System.exit(0));

        HBox bottomButtons = new HBox(10, loginButton, exitButton);
        VBox loginBox = new VBox(10, userLabel, username, passLabel, password, bottomButtons, status);
        loginBox.setPadding(new Insets(20));

        Scene scene = new Scene(loginBox, 300, 200);
        primaryStage.setTitle("Login");
        primaryStage.setScene(scene);
        primaryStage.show();

        loginButton.setOnAction(e -> {
            String user = username.getText().trim();
            String pass = password.getText().trim();

            if (user.equals("admin") && pass.equals("Admin321")) {
                new AdminDashboard().start(new Stage());
                primaryStage.close();
                return;
            }

            String encryptedInput = PasswordUtils.encrypt(pass);
            if (UserJDBC.validateUser(user, encryptedInput)) {
                EventTableViewer.setUsername(user);
                try {
                    new EventTableViewer().start(new Stage());
                    primaryStage.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                status.setText("Invalid username or password.");
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
