module org.letakti.todolist {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;


    opens io.letakti.todolist to javafx.fxml;
    exports io.letakti.todolist;
    exports io.letakti.todolist.controller;
    opens io.letakti.todolist.controller to javafx.fxml;
}