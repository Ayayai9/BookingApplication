module org.example.assignment {
    requires javafx.controls;
    requires javafx.fxml;
    requires junit4;
    requires java.sql;


    opens org.example.assignment to javafx.fxml;
    exports org.example.assignment;
    exports controller;
    opens controller to javafx.fxml;
    exports model;
    opens model to javafx.fxml;
    exports databse;
    opens databse to javafx.fxml;
    exports testing;
    opens testing to javafx.fxml;
    exports util;
    opens util to javafx.fxml;
}