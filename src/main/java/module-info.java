module org.letakti.todolist {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires com.google.gson;

    opens org.letakti.todolist to javafx.fxml, com.google.gson;
    exports org.letakti.todolist;
}