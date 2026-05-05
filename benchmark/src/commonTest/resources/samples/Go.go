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

type TaskRepository struct{ tasks []Task }

func NewTaskRepository() *TaskRepository {
	return &TaskRepository{tasks: make([]Task, 0)}
}

func (r *TaskRepository) Add(title string, priority Priority,
	tags []string, dueDate *string) {
	if title == "" { panic("Title must not be blank") }
	r.tasks = append(r.tasks, Task{
		ID: fmt.Sprintf("uuid-%d", len(r.tasks)), Title: title,
		Completed: false, Priority: priority, Tags: tags, DueDate: dueDate,
	})
}

func (r *TaskRepository) Complete(id string) bool {
	for i := range r.tasks {
		if r.tasks[i].ID == id { r.tasks[i].Completed = true; return true }
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
	sorted := make([]Task, len(r.tasks)); copy(sorted, r.tasks)
	sort.Slice(sorted, func(i, j int) bool {
		if sorted[i].DueDate == nil { return false }
		if sorted[j].DueDate == nil { return true }
		return *sorted[i].DueDate < *sorted[j].DueDate
	})
	return sorted
}

func (r *TaskRepository) Summary() map[string]int {
	summary := map[string]int{"total": len(r.tasks), "completed": 0, "pending": 0}
	for _, task := range r.tasks {
		if task.Completed { summary["completed"]++ } else { summary["pending"]++ }
	}
	return summary
}

func main() {
	repo := NewTaskRepository()
	dec25, nov1 := "2024-12-25", "2024-11-01"

	repo.Add("Buy groceries", High, []string{"shopping"}, &dec25)
	repo.Add("Write report", Medium, []string{"work"}, nil)
	repo.Add("Call dentist", Low, []string{"health"}, &nov1)

	fmt.Println("Overdue tasks:")
	for _, task := range repo.Overdue() { fmt.Printf(" - %s\n", task.Title) }

	summary := repo.Summary()
	fmt.Printf("Total: %d, Done: %d, Todo: %d\n",
		summary["total"], summary["completed"], summary["pending"])
}
