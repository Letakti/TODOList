/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.letakti.todolist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 *
 * @author Letakti
 */
public class TODOListController implements Initializable {

    @FXML
    private TextField tfAdd;

    @FXML
    private ListView<CheckBox> lwMain;

    @FXML
    private void handleAddButton(ActionEvent event) {
        String input = tfAdd.getText();

        if (input == null || input.trim().isEmpty()) {
            return;
        }

        CheckBox cbAdd = new CheckBox(input);
        lwMain.getItems().add(cbAdd);
        tfAdd.clear();
    }

    @FXML
    private void handleDeleteButton(ActionEvent event) {
        int index = lwMain.getSelectionModel().getSelectedIndex();
        if (index < 0) {
            return;
        }
        lwMain.getItems().remove(index);

    }

    @FXML
    private void handleDoneButton(ActionEvent event) {
        int index = lwMain.getSelectionModel().getSelectedIndex();
        if (index < 0) {
            return;
        }

        lwMain.getItems().get(index).setSelected(true);
    }

    @FXML
    public void saveTasks() {
        List<CheckBox> allItems = lwMain.getItems();

        File saveFile = new File(System.getProperty("user.home"), "todolist_saved.txt");

        try (java.io.FileWriter fw = new java.io.FileWriter(saveFile)) {
            for (CheckBox cb : allItems) {
                String value = cb.getText().replace("\n", " ").replace(";", ",");
                boolean isChecked = cb.isSelected();
                fw.write(value + ";" + isChecked + "\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(TODOListController.class.getName()).log(Level.SEVERE, "Failed to save tasks", ex);
        }
    }

    @FXML
    public void loadTasks() {
        File saveFile = new File(System.getProperty("user.home"), "todolist_saved.txt");
        if (!saveFile.exists()) {
            return;
        }

        try (Scanner sc = new Scanner(saveFile)) {
            while (sc.hasNextLine()) {
                String next = sc.nextLine();
                String[] split = next.split(";", 2);
                if (split.length != 2) {
                    continue;
                }

                CheckBox load = new CheckBox(split[0]);
                load.setSelected(Boolean.parseBoolean(split[1]));
                lwMain.getItems().add(load);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TODOListController.class.getName()).log(Level.SEVERE, "Failed to load tasks", ex);
        }
    }

    @FXML
    private void handleCloseMenuButton(ActionEvent event) {
        saveTasks();
        Stage stage = (Stage) lwMain.getScene().getWindow();
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
            lwMain.getItems().clear();
        }

    }

    @FXML
    private void handleNewTaskMenuButton(ActionEvent event) {
        tfAdd.requestFocus();
    }

    @FXML
    private void handleDeleteDoneTasks(ActionEvent event) {
        lwMain.getItems().removeIf(CheckBox::isSelected);
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
        loadTasks();

        lwMain.setCellFactory(lv -> new ListCell<CheckBox>() {
            @Override
            protected void updateItem(CheckBox item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(item);

                }
            }
        });
    }

}
