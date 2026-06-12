package ReminderAppParentFolder.Session;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure data container for the task list and overlay setting.
 * No logic — only fields, getters, and setters.
 */
public class TaskList {

    private List<String> tasks      = new ArrayList<String>();

    public TaskList() {}

    // ── Getters ────────────────────────────────────────────────────────────────

    public List<String> getTasks()      { return tasks; }


    // ── Setters ────────────────────────────────────────────────────────────────

    public void setTasks(List<String> tasks)    { this.tasks      = tasks != null ? new ArrayList<>(tasks) : new ArrayList<>(); }
}
