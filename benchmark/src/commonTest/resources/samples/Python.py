from abc import ABC, abstractmethod
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

class Repository(ABC):
    @abstractmethod
    def add(self, title: str, **kwargs) -> Task:
        pass

class TaskRepository(Repository):
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
        total = len(self.tasks)
        completed = sum(1 for t in self.tasks if t.completed)
        return {
            "total": total,
            "completed": completed,
            "pending": total - completed,
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
