/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.letakti.todolist;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author Letakti
 */
public class TODOListController implements Initializable {

    private static final double ACTION_HOVER_ZONE_WIDTH = 80.0;
    private static final int PRIORITY_HIGH = 2;
    private static final int PRIORITY_NORMAL = 1;
    private static final int PRIORITY_LOW = 0;

    private static final String PRIORITY_CLASS_HIGH = "priority-high";
    private static final String PRIORITY_CLASS_NORMAL = "priority-normal";
    private static final String PRIORITY_CLASS_LOW = "priority-low";

    private static final String THEME_LIGHT = "theme-light";
    private static final String THEME_DARK = "theme-dark";

    private static final String ACCENT_MINT = "accent-mint";
    private static final String ACCENT_BLUE = "accent-blue";
    private static final String ACCENT_ORANGE = "accent-orange";

    private final TaskStorage storage = new TaskStorage();
    private final SettingsStorage settingsStorage = new SettingsStorage();
    private final PauseTransition saveDebounce = new PauseTransition(Duration.millis(400));
    private boolean suppressAutoSave = false;
    private AppSettings settings;

    @FXML
    private TextField tfAdd;

    @FXML
    private ListView<CheckBox> lwMain;

    @FXML
    private MenuBar menuBar;

    @FXML
    private void handleAddButton(ActionEvent event) {
        String input = tfAdd.getText();

        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String now = nowIso();
        TaskData data = new TaskData(input.trim(), false, now, now, PRIORITY_NORMAL);
        CheckBox cbAdd = buildCheckBox(data);
        lwMain.getItems().add(cbAdd);
        tfAdd.clear();
        sortItems();
        requestSave();
    }

    @FXML
    private void handleDeleteButton(ActionEvent event) {
        int index = lwMain.getSelectionModel().getSelectedIndex();
        if (index < 0) {
            return;
        }
        lwMain.getItems().remove(index);
        requestSave();

    }

    @FXML
    private void handleDoneButton(ActionEvent event) {
        int index = lwMain.getSelectionModel().getSelectedIndex();
        if (index < 0) {
            return;
        }

        lwMain.getItems().get(index).setSelected(true);
        requestSave();
    }

    @FXML
    public void saveTasks() {
        saveTasksImmediate();
    }

    @FXML
    public void loadTasks() {
        suppressAutoSave = true;
        List<TaskData> tasks = storage.loadTasks();
        lwMain.getItems().clear();
        for (TaskData data : tasks) {
            lwMain.getItems().add(buildCheckBox(data));
        }
        sortItems();
        suppressAutoSave = false;
    }

    private void saveTasksImmediate() {
        if (suppressAutoSave) {
            return;
        }
        List<TaskData> tasks = new ArrayList<>();
        for (CheckBox cb : lwMain.getItems()) {
            TaskData data = getTaskData(cb);
            data.text = cb.getText();
            data.done = cb.isSelected();
            if (data.createdAt == null) {
                data.createdAt = nowIso();
            }
            if (data.updatedAt == null) {
                data.updatedAt = nowIso();
            }
            tasks.add(data);
        }
        storage.saveTasks(tasks);
    }

    private void requestSave() {
        if (suppressAutoSave) {
            return;
        }
        saveDebounce.setOnFinished(event -> saveTasksImmediate());
        saveDebounce.playFromStart();
    }

    private CheckBox buildCheckBox(TaskData data) {
        if (data.createdAt == null) {
            data.createdAt = nowIso();
        }
        if (data.updatedAt == null) {
            data.updatedAt = data.createdAt;
        }
        if (data.priority < PRIORITY_LOW || data.priority > PRIORITY_HIGH) {
            data.priority = PRIORITY_NORMAL;
        }
        CheckBox checkBox = new CheckBox(data.text);
        checkBox.setSelected(data.done);
        checkBox.setUserData(data);
        checkBox.selectedProperty().addListener((obs, oldValue, newValue) -> {
            data.done = newValue;
            data.updatedAt = nowIso();
            requestSave();
        });
        return checkBox;
    }

    private TaskData getTaskData(CheckBox cb) {
        Object data = cb.getUserData();
        if (data instanceof TaskData) {
            return (TaskData) data;
        }
        String now = nowIso();
        TaskData created = new TaskData(cb.getText(), cb.isSelected(), now, now, PRIORITY_NORMAL);
        cb.setUserData(created);
        cb.selectedProperty().addListener((obs, oldValue, newValue) -> {
            created.done = newValue;
            created.updatedAt = nowIso();
            requestSave();
        });
        return created;
    }

    private void editTaskText(CheckBox item) {
        TaskData data = getTaskData(item);
        TextInputDialog dialog = new TextInputDialog(item.getText());
        dialog.setTitle("Редактирование");
        dialog.setHeaderText(null);
        dialog.setContentText("Текст задачи:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        String newText = result.get().trim();
        if (newText.isEmpty()) {
            return;
        }
        item.setText(newText);
        data.text = newText;
        data.updatedAt = nowIso();
        sortItems();
        requestSave();
    }

    private void setPriority(CheckBox item, int priority) {
        TaskData data = getTaskData(item);
        if (data.priority == priority) {
            return;
        }
        data.priority = priority;
        data.updatedAt = nowIso();
        sortItems();
        requestSave();
    }

    private void sortItems() {
        lwMain.getItems().sort(Comparator
                .comparingInt((CheckBox cb) -> getTaskData(cb).priority).reversed()
                .thenComparing(cb -> safeString(getTaskData(cb).updatedAt), Comparator.reverseOrder()));
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private void applyPriorityStyle(HBox root, TaskData data) {
        root.getStyleClass().removeAll(PRIORITY_CLASS_HIGH, PRIORITY_CLASS_NORMAL, PRIORITY_CLASS_LOW);
        if (data.priority == PRIORITY_HIGH) {
            root.getStyleClass().add(PRIORITY_CLASS_HIGH);
        } else if (data.priority == PRIORITY_LOW) {
            root.getStyleClass().add(PRIORITY_CLASS_LOW);
        } else {
            root.getStyleClass().add(PRIORITY_CLASS_NORMAL);
        }
    }

    private void handleListKeyPressed(KeyEvent event) {
        CheckBox selected = lwMain.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (event.getCode() == KeyCode.F2) {
            editTaskText(selected);
            event.consume();
            return;
        }
        if (event.getCode() == KeyCode.DELETE) {
            lwMain.getItems().remove(selected);
            requestSave();
            event.consume();
            return;
        }
        if (event.isControlDown()) {
            if (event.getCode() == KeyCode.DIGIT1) {
                setPriority(selected, PRIORITY_HIGH);
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.DIGIT2) {
                setPriority(selected, PRIORITY_NORMAL);
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.DIGIT3) {
                setPriority(selected, PRIORITY_LOW);
                event.consume();
            }
        }
    }

    private void showContextMenu(ContextMenuEvent event, ContextMenu menu) {
        if (menu == null) {
            return;
        }
        menu.show(lwMain, event.getScreenX(), event.getScreenY());
        event.consume();
    }

    private void applyThemeToScene(Scene scene) {
        if (scene == null) {
            return;
        }
        ensureStylesheet(scene);
        applyThemeClassesToRoot(scene.getRoot());

        if (isDarkTheme()) {
            scene.setFill(javafx.scene.paint.Color.web("#0f172a"));
        } else {
            scene.setFill(javafx.scene.paint.Color.web("#f5f7fb"));
        }
    }

    private void applyThemeClassesToRoot(Parent root) {
        if (root == null) {
            return;
        }
        root.getStyleClass().removeAll(THEME_LIGHT, THEME_DARK, ACCENT_MINT, ACCENT_BLUE, ACCENT_ORANGE);
        root.getStyleClass().add(isDarkTheme() ? THEME_DARK : THEME_LIGHT);

        String accent = settings != null ? settings.accent : "mint";
        if ("blue".equalsIgnoreCase(accent)) {
            root.getStyleClass().add(ACCENT_BLUE);
        } else if ("orange".equalsIgnoreCase(accent)) {
            root.getStyleClass().add(ACCENT_ORANGE);
        } else {
            root.getStyleClass().add(ACCENT_MINT);
        }
    }

    private void ensureStylesheet(Scene scene) {
        String css = Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm();
        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }
    }

    private void applyThemeToPopup(ContextMenu menu) {
        if (menu == null) {
            return;
        }
        menu.setOnShowing(event -> {
            Scene scene = menu.getScene();
            if (scene == null) {
                return;
            }
            ensureStylesheet(scene);
            applyThemeClassesToRoot(scene.getRoot());
        });
    }

    private void registerMenuThemes(Menu menu) {
        if (menu == null) {
            return;
        }
        menu.setOnShowing(event -> applyThemeToPopup(menu.getParentPopup()));
        for (MenuItem item : menu.getItems()) {
            if (item instanceof Menu) {
                registerMenuThemes((Menu) item);
            }
        }
    }

    private boolean isDarkTheme() {
        String mode = settings != null ? settings.themeMode : "system";
        return "dark".equalsIgnoreCase(mode) || ("system".equalsIgnoreCase(mode) && isSystemDark());
    }

    private boolean isSystemDark() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (!os.contains("win")) {
            return false;
        }
        String theme = System.getProperty("win.theme", "");
        return "dark".equalsIgnoreCase(theme);
    }

    private void setThemeMode(String mode) {
        if (settings == null) {
            settings = new AppSettings("system", "mint");
        }
        settings.themeMode = mode;
        settingsStorage.save(settings);
        Scene scene = lwMain.getScene();
        applyThemeToScene(scene);
    }

    private void setAccent(String accent) {
        if (settings == null) {
            settings = new AppSettings("system", "mint");
        }
        settings.accent = accent;
        settingsStorage.save(settings);
        Scene scene = lwMain.getScene();
        applyThemeToScene(scene);
    }

    private String nowIso() {
        return LocalDateTime.now().toString();
    }

    @FXML
    private void handleEditSelectedTask(ActionEvent event) {
        CheckBox selected = lwMain.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        editTaskText(selected);
    }

    @FXML
    private void handleSetPriorityHigh(ActionEvent event) {
        CheckBox selected = lwMain.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        setPriority(selected, PRIORITY_HIGH);
    }

    @FXML
    private void handleSetPriorityNormal(ActionEvent event) {
        CheckBox selected = lwMain.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        setPriority(selected, PRIORITY_NORMAL);
    }

    @FXML
    private void handleSetPriorityLow(ActionEvent event) {
        CheckBox selected = lwMain.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        setPriority(selected, PRIORITY_LOW);
    }

    @FXML
    private void handleThemeLight(ActionEvent event) {
        setThemeMode("light");
    }

    @FXML
    private void handleThemeDark(ActionEvent event) {
        setThemeMode("dark");
    }

    @FXML
    private void handleThemeSystem(ActionEvent event) {
        setThemeMode("system");
    }

    @FXML
    private void handleAccentMint(ActionEvent event) {
        setAccent("mint");
    }

    @FXML
    private void handleAccentBlue(ActionEvent event) {
        setAccent("blue");
    }

    @FXML
    private void handleAccentOrange(ActionEvent event) {
        setAccent("orange");
    }

    @FXML
    private void handleCloseMenuButton(ActionEvent event) {
        saveTasksImmediate();
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
            requestSave();
        }

    }

    @FXML
    private void handleNewTaskMenuButton(ActionEvent event) {
        tfAdd.requestFocus();
    }

    @FXML
    private void handleDeleteDoneTasks(ActionEvent event) {
        lwMain.getItems().removeIf(CheckBox::isSelected);
        requestSave();
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
        settings = settingsStorage.load();
        loadTasks();
        lwMain.addEventFilter(KeyEvent.KEY_PRESSED, this::handleListKeyPressed);

        if (menuBar != null) {
            for (Menu menu : menuBar.getMenus()) {
                registerMenuThemes(menu);
            }
        }

        lwMain.setCellFactory(lv -> new ListCell<CheckBox>() {
            private final HBox cellRoot;
            private final Region spacer;
            private final HBox actions;
            private final Button doneButton;
            private final Button deleteButton;
            private final ContextMenu contextMenu;

            {
                doneButton = new Button("\u2713");
                doneButton.getStyleClass().addAll("task-action", "task-action-done");
                doneButton.setFocusTraversable(false);

                deleteButton = new Button("\u2715");
                deleteButton.getStyleClass().addAll("task-action", "task-action-delete");
                deleteButton.setFocusTraversable(false);

                actions = new HBox(6, doneButton, deleteButton);
                actions.setPadding(new Insets(0, 6, 0, 6));
                actions.setOpacity(0.0);
                actions.setMouseTransparent(true);
                actions.getStyleClass().add("task-actions");

                spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                cellRoot = new HBox(8);
                cellRoot.getStyleClass().add("task-cell");
                cellRoot.setPadding(new Insets(4, 6, 4, 6));

                MenuItem priorityHigh = new MenuItem("Высокий");
                MenuItem priorityNormal = new MenuItem("Обычный");
                MenuItem priorityLow = new MenuItem("Низкий");
                Menu priorityMenu = new Menu("Приоритет");
                priorityMenu.getItems().addAll(priorityHigh, priorityNormal, priorityLow);

                MenuItem editItem = new MenuItem("Редактировать");
                contextMenu = new ContextMenu(priorityMenu, editItem);
                applyThemeToPopup(contextMenu);

                setOnMouseMoved(event -> updateActionsVisibility(event.getX()));
                setOnMouseExited(event -> hideActions());
                setOnContextMenuRequested(event -> showContextMenu(event, contextMenu));

                setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                        CheckBox item = getItem();
                        if (item != null) {
                            editTaskText(item);
                        }
                    }
                });

                priorityHigh.setOnAction(event -> {
                    CheckBox item = getItem();
                    if (item != null) {
                        setPriority(item, PRIORITY_HIGH);
                    }
                });
                priorityNormal.setOnAction(event -> {
                    CheckBox item = getItem();
                    if (item != null) {
                        setPriority(item, PRIORITY_NORMAL);
                    }
                });
                priorityLow.setOnAction(event -> {
                    CheckBox item = getItem();
                    if (item != null) {
                        setPriority(item, PRIORITY_LOW);
                    }
                });
                editItem.setOnAction(event -> {
                    CheckBox item = getItem();
                    if (item != null) {
                        editTaskText(item);
                    }
                });
            }

            private void updateActionsVisibility(double mouseX) {
                if (getWidth() <= ACTION_HOVER_ZONE_WIDTH || getWidth() - mouseX <= ACTION_HOVER_ZONE_WIDTH) {
                    showActions();
                } else {
                    hideActions();
                }
            }

            private void showActions() {
                if (actions.getOpacity() == 1.0 && !actions.isMouseTransparent()) {
                    return;
                }
                actions.setOpacity(1.0);
                actions.setMouseTransparent(false);
            }

            private void hideActions() {
                if (actions.getOpacity() == 0.0 && actions.isMouseTransparent()) {
                    return;
                }
                actions.setOpacity(0.0);
                actions.setMouseTransparent(true);
            }

            @Override
            protected void updateItem(CheckBox item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setContextMenu(null);
                    cellRoot.getChildren().setAll(spacer, actions);
                    cellRoot.getStyleClass().removeAll(PRIORITY_CLASS_HIGH, PRIORITY_CLASS_NORMAL, PRIORITY_CLASS_LOW);
                } else {
                    cellRoot.getChildren().setAll(item, spacer, actions);

                    TaskData data = getTaskData(item);
                    applyPriorityStyle(cellRoot, data);

                    HBox.setHgrow(item, Priority.ALWAYS);
                    doneButton.setOnAction(event -> {
                        CheckBox current = getItem();
                        if (current == null) {
                            return;
                        }
                        current.setSelected(true);
                        requestSave();
                    });
                    deleteButton.setOnAction(event -> {
                        CheckBox current = getItem();
                        if (current == null) {
                            return;
                        }
                        lwMain.getItems().remove(current);
                        requestSave();
                    });

                    setText(null);
                    setGraphic(cellRoot);
                    setContextMenu(contextMenu);
                    hideActions();
                }
            }
        });

        Scene scene = lwMain.getScene();
        if (scene != null) {
            applyThemeToScene(scene);
        } else {
            lwMain.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    applyThemeToScene(newScene);
                }
            });
        }
    }

}