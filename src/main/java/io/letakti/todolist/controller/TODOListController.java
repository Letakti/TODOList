/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.letakti.todolist.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.letakti.todolist.ui.contorls.view.TaskCell;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import io.letakti.todolist.model.Task;

/**
 *
 * @author Letakti
 */
public class TODOListController implements Initializable {

    @FXML
    private TextField tfAdd;

    @FXML
    private ListView<Task> lvMain;

    @FXML
    private void handleAddButton(ActionEvent event) {
        String input = tfAdd.getText();

        if (input == null || input.trim().isEmpty()) {
            return;
        }

        Task task = new Task(input);
        lvMain.getItems().add(task);
        tfAdd.clear();
    }

    @FXML
    private void handleDeleteButton(ActionEvent event) {
        int index = lvMain.getSelectionModel().getSelectedIndex();
        lvMain.getItems().remove(index);

    }

    @FXML
    private void handleDoneButton(ActionEvent event) {
        int index = lvMain.getSelectionModel().getSelectedIndex();

        lvMain.getItems().get(index).setCompleted(true);
    }

    @FXML
    public void saveTasks() {
        List<Task> allItems = lvMain.getItems();

        try {
            FileWriter fw = new FileWriter(new File(System.getProperty("user.home"), "todolist_saved.txt"));
            for (Task task : allItems) {
                String value = task.getTaskDescription();
                boolean isChecked = task.isCompleted();
                String result = value + ";" + String.valueOf(isChecked) + "\n";
                fw.write(result);
            }
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(TODOListController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void loadTasks(ListView<Task> listView) {
        try (Scanner sc = new Scanner(new File(System.getProperty("user.home"), "todolist_saved.txt"))) {
            while (sc.hasNext()) {
                String next = sc.nextLine();
                String[] splited = next.split(";");

                Task load = new Task(splited[0]);
                load.setCompleted(Boolean.valueOf(splited[1]));
                lvMain.getItems().add(load);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TODOListController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleCloseMenuButton(ActionEvent event) {
        saveTasks();
        Stage stage = (Stage) lvMain.getScene().getWindow();
        stage.hide();
    }

    @FXML
    private void handleDeleteAllButton(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText(null);
        alert.setContentText("Вы уверены, что хотите удалить все задачи?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            lvMain.getItems().clear();
        }

    }

    @FXML
    private void handleNewTaskMenuButton(ActionEvent event) {
        tfAdd.requestFocus();
    }

    @FXML
    private void handleDeleteDoneTasks(ActionEvent event) {
        lvMain.getItems().removeIf(Task::isCompleted);
    }

    @FXML
    private void handleAboutButton(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("О программе");
        alert.setHeaderText(null);
        alert.setGraphic(null);
        alert.setContentText("TODOList v1.0" + "\n" +
                "Автор: Letakti" + "\n" +
                "JavaFX проект для заметок" + "\n" + "\n"
        );
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            alert.close();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadTasks(lvMain);
        lvMain.setEditable(true);
        lvMain.setCellFactory(param -> new TaskCell());

    }

}
