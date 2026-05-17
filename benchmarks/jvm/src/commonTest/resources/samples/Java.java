package com.example.todo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

enum Priority { LOW, MEDIUM, HIGH }

interface Identifiable { UUID getId(); }

class Task implements Identifiable {
    final UUID id; String title; boolean completed;
    Priority priority; List<String> tags; LocalDate dueDate;

    Task(UUID id, String title, Priority priority, List<String> tags, LocalDate dueDate) {
        this.id = id; this.title = title; this.completed = false;
        this.priority = priority;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.dueDate = dueDate;
    }

    public UUID getId() { return id; }
}

class TaskRepository {
    private final List<Task> tasks = new ArrayList<>();

    Task add(String title, Priority priority, List<String> tags, LocalDate dueDate) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title must not be blank");
        }
        Task task = new Task(UUID.randomUUID(), title, priority, tags, dueDate);
        tasks.add(task);
        return task;
    }

    boolean complete(UUID id) {
        for (Task task : tasks) {
            if (task.id.equals(id)) { task.completed = true; return true; }
        }
        return false;
    }

    boolean remove(UUID id) { return tasks.removeIf(t -> t.id.equals(id)); }

    List<Task> filterByPriority(Priority priority) {
        return tasks.stream().filter(t -> t.priority == priority).collect(Collectors.toList());
    }

    List<Task> filterByTag(String tag) {
        return tasks.stream().filter(t -> t.tags.contains(tag)).collect(Collectors.toList());
    }

    List<Task> overdue() {
        return overdue(LocalDate.now());
    }

    List<Task> overdue(LocalDate today) {
        return tasks.stream()
            .filter(t -> t.dueDate != null && t.dueDate.isBefore(today) && !t.completed)
            .collect(Collectors.toList());
    }

    List<Task> sortByDueDate() {
        return tasks.stream()
            .sorted(Comparator.comparing((Task t) -> t.dueDate,
                Comparator.nullsLast(Comparator.naturalOrder())))
            .collect(Collectors.toList());
    }

    long countCompleted() { return tasks.stream().filter(t -> t.completed).count(); }
    Map<String, Integer> summary() {
        Map<String, Integer> map = new HashMap<>();
        map.put("total", tasks.size());
        map.put("completed", (int) countCompleted());
        map.put("pending", (int) (tasks.size() - countCompleted()));
        return map;
    }
}
public class TodoApp {
    public static void main(String[] args) {
        TaskRepository repo = new TaskRepository();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        repo.add("Buy groceries", Priority.HIGH,
            Arrays.asList("shopping"), LocalDate.parse("2024-12-25", fmt));
        repo.add("Write report", Priority.MEDIUM,
            Arrays.asList("work"), null);
        repo.add("Call dentist", Priority.LOW,
            Arrays.asList("health"), LocalDate.parse("2024-11-01", fmt));

        System.out.println("Overdue tasks:");
        repo.overdue().forEach(t -> System.out.println(" - " + t.title));

        Map<String, Integer> summary = repo.summary();
        System.out.printf("Total: %d, Done: %d, Todo: %d%n",
            summary.get("total"), summary.get("completed"), summary.get("pending"));
    }
}
