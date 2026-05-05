import Foundation

enum Priority: String, CaseIterable {
    case low = "LOW"
    case medium = "MEDIUM"
    case high = "HIGH"
}

enum TaskError: Error {
    case emptyTitle
}

protocol Repository {
    associatedtype T
    func add(item: T) throws
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
        guard let index = tasks.firstIndex(where: { $0.id == id }) else { return false }
        tasks[index].completed = true
        return true
    }

    func remove(id: UUID) -> Bool {
        guard let index = tasks.firstIndex(where: { $0.id == id }) else { return false }
        tasks.remove(at: index)
        return true
    }

    func filterByPriority(_ priority: Priority) -> [Task] {
        return tasks.filter { $0.priority == priority }
    }

    func filterByTag(_ tag: String) -> [Task] {
        return tasks.filter { $0.tags.contains(tag) }
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
            "completed": tasks.filter { $0.completed }.count,
            "pending": tasks.filter { !$0.completed }.count
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
repo.overdue().forEach { print(" - \($0.title)") }

let summary = repo.summary()
print("Total: \(summary["total"]!), Done: \(summary["completed"]!), Todo: \(summary["pending"]!)")
