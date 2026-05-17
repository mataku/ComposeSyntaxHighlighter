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

struct TaskRepository { tasks: Vec<Task> }

impl TaskRepository {
    fn new() -> Self {
        TaskRepository { tasks: Vec::new() }
    }

    fn add(&mut self, title: &str, priority: Priority,
           tags: Vec<String>, due_date: Option<String>) -> Task {
        assert!(!title.is_empty(), "Title must not be blank");
        let task = Task {
            id: format!("uuid-{}", self.tasks.len()),
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
        match self.tasks.iter_mut().find(|t| t.id == id) {
            Some(t) => { t.completed = true; true }
            None => false,
        }
    }

    fn remove(&mut self, id: &str) -> bool {
        match self.tasks.iter().position(|t| t.id == id) {
            Some(pos) => { self.tasks.remove(pos); true }
            None => false,
        }
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
        let mut m = HashMap::new();
        m.insert("total".to_string(), self.tasks.len() as i32);
        m.insert("completed".to_string(), self.tasks.iter().filter(|t| t.completed).count() as i32);
        m.insert("pending".to_string(), self.tasks.iter().filter(|t| !t.completed).count() as i32);
        m
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
    for task in repo.overdue() { println!(" - {}", task.title); }

    let s = repo.summary();
    println!("Total: {}, Done: {}, Todo: {}", s["total"], s["completed"], s["pending"]);
}
