package io.letakti.todolist.ui.contorls.view;

import io.letakti.todolist.model.Task;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class TaskCell extends ListCell<Task> {
    private HBox hbox = new HBox(5); // spacing между элементами
    private CheckBox checkBox = new CheckBox();
    private Label label = new Label();
    private TextField textField = new TextField();
    private Button editButton = new Button("✎"); // иконка карандаша

    public TaskCell() {
        super();
        hbox.getChildren().addAll(checkBox, label, editButton);
        editButton.getStyleClass().add("edit-button");


        // Обработчик кнопки редактирования
        editButton.setOnAction(event -> startEdit());

        // Enter или потеря фокуса
        textField.setOnAction(event -> commitEdit(getItem()));
        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                commitEdit(getItem());
            }
        });

        // Синхронизация CheckBox с моделью
        checkBox.setOnAction(event -> {
            if (getItem() != null) {
                getItem().setCompleted(checkBox.isSelected());
            }
        });
    }

    @Override
    protected void updateItem(Task task, boolean empty) {
        super.updateItem(task, empty);
        if (empty || task == null) {
            setText(null);
            setGraphic(null);
        } else {
            checkBox.setSelected(task.isCompleted());
            label.setText(task.getTaskDescription());
            textField.setText(task.getTaskDescription());

            if (isEditing()) {
                hbox.getChildren().set(1, textField); // заменяем Label на TextField
                Platform.runLater(() -> {
                    textField.requestFocus();
                    textField.selectAll();
                });
            } else {
                hbox.getChildren().set(1, label); // возвращаем Label
            }

            setGraphic(hbox);
            label.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(label, Priority.ALWAYS);
            HBox.setHgrow(textField, Priority.ALWAYS);

            hbox.setOnMouseClicked(event -> {
                if (!editButton.equals(event.getTarget())) { // если клик не на кнопке
                    checkBox.setSelected(!checkBox.isSelected());
                    if (getItem() != null) {
                        getItem().setCompleted(checkBox.isSelected());
                    }
                }
            });
        }
    }

    @Override
    public void startEdit() {
        super.startEdit();
        if (getItem() != null) {
            updateItem(getItem(), false);
        }
    }

    @Override
    public void commitEdit(Task task) {
        if (task != null) {
            task.setTaskDescription(textField.getText());
        }
        super.commitEdit(task);
        updateItem(task, false);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        updateItem(getItem(), false);
    }
}
