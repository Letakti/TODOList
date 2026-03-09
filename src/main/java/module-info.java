module org.letakti.todolist {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;


    opens org.letakti.todolist to javafx.fxml;
    exports org.letakti.todolist;
}