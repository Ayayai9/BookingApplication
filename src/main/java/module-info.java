module org.example.assignment {
    requires javafx.controls;
    requires javafx.fxml;
    requires junit4;
    requires java.sql;


    opens org.example.assignment to javafx.fxml;
    exports org.example.assignment;
}