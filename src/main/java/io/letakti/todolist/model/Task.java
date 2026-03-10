package io.letakti.todolist.model;

public class Task {
    private String taskDescription;
    private boolean isCompleted;

    public Task(String taskDescription) {
        this.taskDescription = taskDescription;
        this.isCompleted = false;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    @Override
    public String toString() {
        return taskDescription;
    }
}
