package org.example.assignment2;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

public class LoginApp extends Application {

    @Override
    public void start(Stage primaryStage) {
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

            HashMap<String, String> usersFromFile = loadUsersFromFile();
            String encryptedInput = PasswordUtils.encrypt(pass);

            if (usersFromFile.containsKey(user) && usersFromFile.get(user).equals(encryptedInput)) {
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

    private HashMap<String, String> loadUsersFromFile() {
        HashMap<String, String> users = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("users.dat"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split(";");
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]); // username -> encrypted password
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading users.dat: " + e.getMessage());
        }
        return users;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
