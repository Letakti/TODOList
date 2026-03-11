package org.letakti.todolist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SettingsStorage {
    private static final Logger LOGGER = Logger.getLogger(SettingsStorage.class.getName());
    private static final String SETTINGS_FILE_NAME = "todolist_settings.json";

    private final File settingsFile;
    private final Gson gson;

    public SettingsStorage() {
        File home = new File(System.getProperty("user.home"));
        this.settingsFile = new File(home, SETTINGS_FILE_NAME);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public AppSettings load() {
        if (!settingsFile.exists()) {
            return new AppSettings("system", "mint");
        }
        try {
            String json = Files.readString(settingsFile.toPath(), StandardCharsets.UTF_8);
            AppSettings settings = gson.fromJson(json, AppSettings.class);
            if (settings == null) {
                return new AppSettings("system", "mint");
            }
            if (settings.themeMode == null || settings.themeMode.isBlank()) {
                settings.themeMode = "system";
            }
            if (settings.accent == null || settings.accent.isBlank()) {
                settings.accent = "mint";
            }
            return settings;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load settings", ex);
            return new AppSettings("system", "mint");
        }
    }

    public void save(AppSettings settings) {
        if (settings == null) {
            return;
        }
        try {
            String json = gson.toJson(settings);
            Files.writeString(settingsFile.toPath(), json, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to save settings", ex);
        }
    }
}