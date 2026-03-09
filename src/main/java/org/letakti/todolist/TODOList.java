package org.letakti.todolist;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class TODOList extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/letakti/todolist/FXMLDocument.fxml"));
        Parent root = loader.load();
        TODOListController controller = loader.getController();

        Scene scene = new Scene(root);


        stage.getIcons().add(new Image(getClass().getResourceAsStream("/org/letakti/todolist/icons/icon.png")));
        stage.setTitle("TOSO List");
        stage.setOnCloseRequest((event) -> {
            controller.saveTasks();
        });
        stage.setScene(scene);
        stage.show();
    }
}
