package io.github.mataku.compose.highlight.benchmark

object BenchmarkSamples {
    val Kotlin = """
        package com.example.todo

        import java.time.LocalDate
        import java.time.format.DateTimeFormatter
        import java.util.UUID

        enum class Priority { LOW, MEDIUM, HIGH }

        data class Task(
            val id: UUID,
            val title: String,
            val completed: Boolean = false,
            val priority: Priority = Priority.MEDIUM,
            val tags: List<String> = emptyList(),
            val dueDate: LocalDate? = null
        )

        class TaskRepository {
            private val tasks = mutableListOf<Task>()

            fun add(title: String, priority: Priority = Priority.MEDIUM,
                    tags: List<String> = emptyList(), dueDate: LocalDate? = null): Task {
                require(title.isNotBlank()) { "Title must not be blank" }
                val task = Task(UUID.randomUUID(), title, false, priority, tags, dueDate)
                tasks.add(task)
                return task
            }

            fun complete(id: UUID): Boolean {
                val index = tasks.indexOfFirst { it.id == id }
                if (index == -1) return false
                tasks[index] = tasks[index].copy(completed = true)
                return true
            }

            fun remove(id: UUID): Boolean = tasks.removeIf { it.id == id }

            fun filterByPriority(priority: Priority): List<Task> =
                tasks.filter { it.priority == priority }

            fun filterByTag(tag: String): List<Task> =
                tasks.filter { it.tags.contains(tag) }

            fun overdue(): List<Task> {
                val today = LocalDate.now()
                return tasks.filter {
                    it.dueDate != null && it.dueDate.isBefore(today) && !it.completed
                }
            }

            fun sortByDueDate(): List<Task> =
                tasks.sortedWith(compareBy(nullsLast()) { it.dueDate })

            fun summary(): Map<String, Int> = mapOf(
                "total" to tasks.size,
                "completed" to tasks.count { it.completed },
                "pending" to tasks.count { !it.completed }
            )
        }

        fun main() {
            val repo = TaskRepository()
            val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            repo.add("Buy groceries", Priority.HIGH,
                listOf("shopping"), LocalDate.parse("2024-12-25", fmt))
            repo.add("Write report", Priority.MEDIUM, listOf("work"))
            repo.add("Call dentist", Priority.LOW,
                listOf("health"), LocalDate.parse("2024-11-01", fmt))

            println("Overdue tasks:")
            repo.overdue().forEach { println(" - ${'$'}{it.title}") }

            val s = repo.summary()
            println("Total: ${'$'}{s["total"]}, Done: ${'$'}{s["completed"]}, Todo: ${'$'}{s["pending"]}")
        }
    """.trimIndent()

    val Swift = """
        import Foundation

        enum Priority: String, CaseIterable {
            case low = "LOW"
            case medium = "MEDIUM"
            case high = "HIGH"
        }

        struct Task {
            let id: UUID
            var title: String
            var completed: Bool = false
            var priority: Priority = .medium
            var tags: [String] = []
            var dueDate: Date? = nil
        }

        class TaskRepository {
            private var tasks: [Task] = []

            func add(title: String, priority: Priority = .medium,
                     tags: [String] = [], dueDate: Date? = nil) -> Task {
                guard !title.isEmpty else { fatalError("Title must not be blank") }
                let task = Task(id: UUID(), title: title, completed: false,
                                priority: priority, tags: tags, dueDate: dueDate)
                tasks.append(task)
                return task
            }

            func complete(id: UUID) -> Bool {
                guard let index = tasks.firstIndex(where: { ${'$'}0.id == id }) else { return false }
                tasks[index].completed = true
                return true
            }

            func remove(id: UUID) -> Bool {
                guard let index = tasks.firstIndex(where: { ${'$'}0.id == id }) else { return false }
                tasks.remove(at: index)
                return true
            }

            func filterByPriority(_ priority: Priority) -> [Task] {
                return tasks.filter { ${'$'}0.priority == priority }
            }

            func filterByTag(_ tag: String) -> [Task] {
                return tasks.filter { ${'$'}0.tags.contains(tag) }
            }

            func overdue() -> [Task] {
                let today = Date()
                return tasks.filter { task in
                    guard let due = task.dueDate, !task.completed else { return false }
                    return due < today
                }
            }

            func sortByDueDate() -> [Task] {
                return tasks.sorted { (a, b) in
                    switch (a.dueDate, b.dueDate) {
                    case let (d1?, d2?): return d1 < d2
                    case (nil, _): return false
                    default: return true
                    }
                }
            }

            func summary() -> [String: Int] {
                return [
                    "total": tasks.count,
                    "completed": tasks.filter { ${'$'}0.completed }.count,
                    "pending": tasks.filter { !${'$'}0.completed }.count
                ]
            }
        }

        let repo = TaskRepository()
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"

        repo.add(title: "Buy groceries", priority: .high,
                 tags: ["shopping"], dueDate: formatter.date(from: "2024-12-25"))
        repo.add(title: "Write report", priority: .medium, tags: ["work"])
        repo.add(title: "Call dentist", priority: .low,
                 tags: ["health"], dueDate: formatter.date(from: "2024-11-01"))

        print("Overdue tasks:")
        repo.overdue().forEach { print(" - \\(${'$'}0.title)") }

        let summary = repo.summary()
        print("Total: \\(summary["total"]!), Done: \\(summary["completed"]!), Todo: \\(summary["pending"]!)")
    """.trimIndent()

    val Ruby = """
        require 'date'
        require 'securerandom'

        module Todo
          class Priority
            LOW = :low
            MEDIUM = :medium
            HIGH = :high
          end

          class Task
            attr_accessor :id, :title, :completed, :priority, :tags, :due_date

            def initialize(id:, title:, priority: Priority::MEDIUM, tags: [], due_date: nil)
              @id = id
              @title = title
              @completed = false
              @priority = priority
              @tags = tags
              @due_date = due_date
            end
          end

          class TaskRepository
            def initialize
              @tasks = []
            end

            def add(title, priority = Priority::MEDIUM, tags = [], due_date = nil)
              raise ArgumentError, "Title must not be blank" if title.nil? || title.strip.empty?
              task = Task.new(
                id: SecureRandom.uuid,
                title: title,
                priority: priority,
                tags: tags,
                due_date: due_date
              )
              @tasks << task
              task
            end

            def complete(id)
              task = @tasks.find { |t| t.id == id }
              return false unless task
              task.completed = true
              true
            end

            def remove(id)
              @tasks.reject! { |t| t.id == id }
              true
            end

            def filter_by_priority(priority)
              @tasks.select { |t| t.priority == priority }
            end

            def filter_by_tag(tag)
              @tasks.select { |t| t.tags.include?(tag) }
            end

            def overdue
              today = Date.today
              @tasks.select { |t| !t.completed && !t.due_date.nil? && t.due_date < today }
            end

            def sort_by_due_date
              @tasks.sort_by { |t| t.due_date || Date.new(9999, 12, 31) }
            end

            def summary
              {
                total: @tasks.size,
                completed: @tasks.count(&:completed),
                pending: @tasks.count { |t| !t.completed }
              }
            end
          end
        end

        repo = Todo::TaskRepository.new

        repo.add("Buy groceries", Todo::Priority::HIGH, ["shopping"], Date.parse("2024-12-25"))
        repo.add("Write report", Todo::Priority::MEDIUM, ["work"])
        repo.add("Call dentist", Todo::Priority::LOW, ["health"], Date.parse("2024-11-01"))

        puts "Overdue tasks:"
        repo.overdue.each { |t| puts " - #{t.title}" }

        summary = repo.summary
        puts "Total: #{summary[:total]}, Done: #{summary[:completed]}, Todo: #{summary[:pending]}"
    """.trimIndent()

    val Rust = """
        use std::collections::HashMap;

        #[derive(Debug, Clone, PartialEq)]
        enum Priority {
            Low,
            Medium,
            High,
        }

        #[derive(Debug, Clone)]
        struct Task {
            id: String,
            title: String,
            completed: bool,
            priority: Priority,
            tags: Vec<String>,
            due_date: Option<String>,
        }

        struct TaskRepository {
            tasks: Vec<Task>,
        }

        impl TaskRepository {
            fn new() -> Self {
                TaskRepository { tasks: Vec::new() }
            }

            fn add(&mut self, title: &str, priority: Priority,
                   tags: Vec<String>, due_date: Option<String>) -> Task {
                assert!(!title.is_empty(), "Title must not be blank");
                let task = Task {
                    id: format!("uuid-{}\", self.tasks.len()),
                    title: title.to_string(),
                    completed: false,
                    priority,
                    tags,
                    due_date,
                };
                self.tasks.push(task.clone());
                task
            }

            fn complete(&mut self, id: &str) -> bool {
                if let Some(task) = self.tasks.iter_mut().find(|t| t.id == id) {
                    task.completed = true;
                    true
                } else {
                    false
                }
            }

            fn remove(&mut self, id: &str) -> bool {
                if let Some(pos) = self.tasks.iter().position(|t| t.id == id) {
                    self.tasks.remove(pos);
                    true
                } else {
                    false
                }
            }

            fn filter_by_priority(&self, priority: &Priority) -> Vec<&Task> {
                self.tasks.iter().filter(|t| &t.priority == priority).collect()
            }

            fn filter_by_tag(&self, tag: &str) -> Vec<&Task> {
                self.tasks.iter().filter(|t| t.tags.contains(&tag.to_string())).collect()
            }

            fn overdue(&self) -> Vec<&Task> {
                let today = "2024-12-01";
                self.tasks.iter().filter(|t| {
                    match (&t.due_date, t.completed) {
                        (Some(due), false) => due.as_str() < today,
                        _ => false,
                    }
                }).collect()
            }

            fn sort_by_due_date(&self) -> Vec<&Task> {
                let mut sorted: Vec<&Task> = self.tasks.iter().collect();
                sorted.sort_by(|a, b| a.due_date.cmp(&b.due_date));
                sorted
            }

            fn summary(&self) -> HashMap<String, i32> {
                let mut map = HashMap::new();
                map.insert("total".to_string(), self.tasks.len() as i32);
                map.insert("completed".to_string(),
                    self.tasks.iter().filter(|t| t.completed).count() as i32);
                map.insert("pending".to_string(),
                    self.tasks.iter().filter(|t| !t.completed).count() as i32);
                map
            }
        }

        fn main() {
            let mut repo = TaskRepository::new();

            repo.add("Buy groceries", Priority::High,
                vec!["shopping".to_string()], Some("2024-12-25".to_string()));
            repo.add("Write report", Priority::Medium,
                vec!["work".to_string()], None);
            repo.add("Call dentist", Priority::Low,
                vec!["health".to_string()], Some("2024-11-01".to_string()));

            println!("Overdue tasks:");
            for task in repo.overdue() {
                println!(" - {}", task.title);
            }

            let summary = repo.summary();
            println!("Total: {}, Done: {}, Todo: {}",
                summary["total"], summary["completed"], summary["pending"]);
        }
    """.trimIndent()

    val Python = """
        from dataclasses import dataclass, field
        from datetime import date
        from enum import Enum
        from typing import Optional, List, Dict
        import uuid

        class Priority(Enum):
            LOW = "low"
            MEDIUM = "medium"
            HIGH = "high"

        @dataclass
        class Task:
            id: uuid.UUID
            title: str
            completed: bool = False
            priority: Priority = Priority.MEDIUM
            tags: List[str] = field(default_factory=list)
            due_date: Optional[date] = None

        class TaskRepository:
            def __init__(self):
                self.tasks: List[Task] = []

            def add(self, title: str, priority: Priority = Priority.MEDIUM,
                    tags: List[str] = None, due_date: Optional[date] = None) -> Task:
                if not title.strip():
                    raise ValueError("Title must not be blank")
                tags = tags or []
                task = Task(
                    id=uuid.uuid4(),
                    title=title,
                    priority=priority,
                    tags=tags,
                    due_date=due_date
                )
                self.tasks.append(task)
                return task

            def complete(self, task_id: uuid.UUID) -> bool:
                for task in self.tasks:
                    if task.id == task_id:
                        task.completed = True
                        return True
                return False

            def remove(self, task_id: uuid.UUID) -> bool:
                for i, task in enumerate(self.tasks):
                    if task.id == task_id:
                        del self.tasks[i]
                        return True
                return False

            def filter_by_priority(self, priority: Priority) -> List[Task]:
                return [t for t in self.tasks if t.priority == priority]

            def filter_by_tag(self, tag: str) -> List[Task]:
                return [t for t in self.tasks if tag in t.tags]

            def overdue(self) -> List[Task]:
                today = date.today()
                return [
                    t for t in self.tasks
                    if t.due_date is not None and t.due_date < today and not t.completed
                ]

            def sort_by_due_date(self) -> List[Task]:
                return sorted(self.tasks, key=lambda t: t.due_date or date.max)

            def summary(self) -> Dict[str, int]:
                return {
                    "total": len(self.tasks),
                    "completed": sum(1 for t in self.tasks if t.completed),
                    "pending": sum(1 for t in self.tasks if not t.completed),
                }

        def main():
            repo = TaskRepository()

            repo.add("Buy groceries", Priority.HIGH, ["shopping"], date(2024, 12, 25))
            repo.add("Write report", Priority.MEDIUM, ["work"])
            repo.add("Call dentist", Priority.LOW, ["health"], date(2024, 11, 1))

            print("Overdue tasks:")
            for task in repo.overdue():
                print(f" - {task.title}")

            summary = repo.summary()
            print(f"Total: {summary['total']}, Done: {summary['completed']}, Todo: {summary['pending']}")

        if __name__ == "__main__":
            main()
    """.trimIndent()

    val Go = """
        package main

        import (
            "fmt"
            "sort"
        )

        type Priority string

        const (
            Low    Priority = "LOW"
            Medium Priority = "MEDIUM"
            High   Priority = "HIGH"
        )

        type Task struct {
            ID        string
            Title     string
            Completed bool
            Priority  Priority
            Tags      []string
            DueDate   *string
        }

        type TaskRepository struct {
            tasks []Task
        }

        func NewTaskRepository() *TaskRepository {
            return &TaskRepository{tasks: make([]Task, 0)}
        }

        func (r *TaskRepository) Add(title string, priority Priority,
            tags []string, dueDate *string) Task {
            if title == "" {
                panic("Title must not be blank")
            }
            task := Task{
                ID:        fmt.Sprintf("uuid-%d", len(r.tasks)),
                Title:     title,
                Completed: false,
                Priority:  priority,
                Tags:      tags,
                DueDate:   dueDate,
            }
            r.tasks = append(r.tasks, task)
            return task
        }

        func (r *TaskRepository) Complete(id string) bool {
            for i := range r.tasks {
                if r.tasks[i].ID == id {
                    r.tasks[i].Completed = true
                    return true
                }
            }
            return false
        }

        func (r *TaskRepository) Remove(id string) bool {
            for i, task := range r.tasks {
                if task.ID == id {
                    r.tasks = append(r.tasks[:i], r.tasks[i+1:]...)
                    return true
                }
            }
            return false
        }

        func (r *TaskRepository) FilterByPriority(priority Priority) []Task {
            result := make([]Task, 0)
            for _, task := range r.tasks {
                if task.Priority == priority {
                    result = append(result, task)
                }
            }
            return result
        }

        func (r *TaskRepository) FilterByTag(tag string) []Task {
            result := make([]Task, 0)
            for _, task := range r.tasks {
                for _, t := range task.Tags {
                    if t == tag {
                        result = append(result, task)
                        break
                    }
                }
            }
            return result
        }

        func (r *TaskRepository) Overdue() []Task {
            today := "2024-12-01"
            result := make([]Task, 0)
            for _, task := range r.tasks {
                if task.DueDate != nil && *task.DueDate < today && !task.Completed {
                    result = append(result, task)
                }
            }
            return result
        }

        func (r *TaskRepository) SortByDueDate() []Task {
            sorted := make([]Task, len(r.tasks))
            copy(sorted, r.tasks)
            sort.Slice(sorted, func(i, j int) bool {
                if sorted[i].DueDate == nil {
                    return false
                }
                if sorted[j].DueDate == nil {
                    return true
                }
                return *sorted[i].DueDate < *sorted[j].DueDate
            })
            return sorted
        }

        func (r *TaskRepository) Summary() map[string]int {
            summary := map[string]int{
                "total":     len(r.tasks),
                "completed": 0,
                "pending":   0,
            }
            for _, task := range r.tasks {
                if task.Completed {
                    summary["completed"]++
                } else {
                    summary["pending"]++
                }
            }
            return summary
        }

        func main() {
            repo := NewTaskRepository()

            dec25 := "2024-12-25"
            nov1 := "2024-11-01"

            repo.Add("Buy groceries", High, []string{"shopping"}, &dec25)
            repo.Add("Write report", Medium, []string{"work"}, nil)
            repo.Add("Call dentist", Low, []string{"health"}, &nov1)

            fmt.Println("Overdue tasks:")
            for _, task := range repo.Overdue() {
                fmt.Printf(" - %s\\n", task.Title)
            }

            summary := repo.Summary()
            fmt.Printf("Total: %d, Done: %d, Todo: %d\\n",
                summary["total"], summary["completed"], summary["pending"])
        }
    """.trimIndent()

    val Java = """
        package com.example.todo;

        import java.time.LocalDate;
        import java.time.format.DateTimeFormatter;
        import java.util.*;
        import java.util.stream.Collectors;

        enum Priority {
            LOW, MEDIUM, HIGH
        }

        class Task {
            final UUID id;
            String title;
            boolean completed;
            Priority priority;
            List<String> tags;
            LocalDate dueDate;

            Task(UUID id, String title, Priority priority,
                 List<String> tags, LocalDate dueDate) {
                this.id = id;
                this.title = title;
                this.completed = false;
                this.priority = priority;
                this.tags = tags != null ? tags : new ArrayList<>();
                this.dueDate = dueDate;
            }
        }

        class TaskRepository {
            private final List<Task> tasks = new ArrayList<>();

            Task add(String title, Priority priority,
                     List<String> tags, LocalDate dueDate) {
                if (title == null || title.trim().isEmpty()) {
                    throw new IllegalArgumentException("Title must not be blank");
                }
                Task task = new Task(UUID.randomUUID(), title,
                    priority, tags, dueDate);
                tasks.add(task);
                return task;
            }

            boolean complete(UUID id) {
                for (Task task : tasks) {
                    if (task.id.equals(id)) {
                        task.completed = true;
                        return true;
                    }
                }
                return false;
            }

            boolean remove(UUID id) {
                return tasks.removeIf(task -> task.id.equals(id));
            }

            List<Task> filterByPriority(Priority priority) {
                return tasks.stream()
                    .filter(task -> task.priority == priority)
                    .collect(Collectors.toList());
            }

            List<Task> filterByTag(String tag) {
                return tasks.stream()
                    .filter(task -> task.tags.contains(tag))
                    .collect(Collectors.toList());
            }

            List<Task> overdue() {
                LocalDate today = LocalDate.now();
                return tasks.stream()
                    .filter(task -> task.dueDate != null
                        && task.dueDate.isBefore(today)
                        && !task.completed)
                    .collect(Collectors.toList());
            }

            List<Task> sortByDueDate() {
                return tasks.stream()
                    .sorted(Comparator.comparing(
                        (Task t) -> t.dueDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
            }

            Map<String, Integer> summary() {
                Map<String, Integer> map = new HashMap<>();
                map.put("total", tasks.size());
                map.put("completed", (int) tasks.stream()
                    .filter(t -> t.completed).count());
                map.put("pending", (int) tasks.stream()
                    .filter(t -> !t.completed).count());
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
                repo.overdue().forEach(task ->
                    System.out.println(" - " + task.title));

                Map<String, Integer> summary = repo.summary();
                System.out.printf("Total: %d, Done: %d, Todo: %d%n",
                    summary.get("total"), summary.get("completed"),
                    summary.get("pending"));
            }
        }
    """.trimIndent()
}
