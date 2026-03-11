package org.letakti.todolist;

public class AppSettings {
    public String themeMode; // light, dark, system
    public String accent;    // mint, blue, orange

    public AppSettings() {
    }

    public AppSettings(String themeMode, String accent) {
        this.themeMode = themeMode;
        this.accent = accent;
    }
}