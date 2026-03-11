package org.letakti.todolist;

public class TaskData {
    public String text;
    public boolean done;
    public String createdAt;
    public String updatedAt;
    public int priority;

    public TaskData() {
    }

    public TaskData(String text, boolean done, String createdAt, String updatedAt) {
        this(text, done, createdAt, updatedAt, 0);
    }

    public TaskData(String text, boolean done, String createdAt, String updatedAt, int priority) {
        this.text = text;
        this.done = done;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.priority = priority;
    }
}