package org.letakti.todolist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaskStorage {
    private static final Logger LOGGER = Logger.getLogger(TaskStorage.class.getName());
    private static final String JSON_FILE_NAME = "todolist_saved.json";
    private static final String LEGACY_FILE_NAME = "todolist_saved.txt";

    private final File jsonFile;
    private final File legacyFile;
    private final Gson gson;

    public TaskStorage() {
        File home = new File(System.getProperty("user.home"));
        this.jsonFile = new File(home, JSON_FILE_NAME);
        this.legacyFile = new File(home, LEGACY_FILE_NAME);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public List<TaskData> loadTasks() {
        if (jsonFile.exists()) {
            return readJson();
        }

        if (legacyFile.exists()) {
            List<TaskData> migrated = loadFromLegacyTxt();
            saveTasks(migrated);
            if (!legacyFile.delete()) {
                LOGGER.log(Level.WARNING, "Failed to delete legacy file");
            }
            return migrated;
        }

        return new ArrayList<>();
    }

    public void saveTasks(List<TaskData> tasks) {
        TaskStore store = new TaskStore();
        store.tasks.addAll(tasks);
        String json = gson.toJson(store);
        try {
            Files.writeString(jsonFile.toPath(), json, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to save tasks", ex);
        }
    }

    private List<TaskData> readJson() {
        try {
            String json = Files.readString(jsonFile.toPath(), StandardCharsets.UTF_8);
            TaskStore store = gson.fromJson(json, TaskStore.class);
            if (store == null || store.tasks == null) {
                return new ArrayList<>();
            }
            return store.tasks;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load tasks", ex);
        }
        return new ArrayList<>();
    }

    private List<TaskData> loadFromLegacyTxt() {
        List<TaskData> tasks = new ArrayList<>();
        try (Scanner sc = new Scanner(legacyFile, StandardCharsets.UTF_8)) {
            while (sc.hasNextLine()) {
                String next = sc.nextLine();
                String[] split = next.split(";", 2);
                if (split.length != 2) {
                    continue;
                }
                String now = nowIso();
                TaskData data = new TaskData(split[0], Boolean.parseBoolean(split[1]), now, now, 0);
                tasks.add(data);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load legacy tasks", ex);
        }
        return tasks;
    }

    private String nowIso() {
        return LocalDateTime.now().toString();
    }

    private static class TaskStore {
        private final List<TaskData> tasks = new ArrayList<>();
    }
}