package com.example.todo

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

enum class Priority { LOW, MEDIUM, HIGH }

sealed class Result<out T>
data class Success<T>(val value: T) : Result<T>()
data class Failure(val error: String) : Result<Nothing>()

data class Task(
    val id: UUID,
    val title: String,
    val completed: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val tags: List<String> = emptyList(),
    val dueDate: LocalDate? = null
)

interface Repository<T> {
    fun add(item: T): Result<T>
    fun remove(id: UUID): Boolean
}

class TaskRepository : Repository<Task> {
    private val tasks = mutableListOf<Task>()

    override fun add(item: Task): Result<Task> {
        require(item.title.isNotBlank()) { "Title must not be blank" }
        tasks.add(item)
        return Success(item)
    }

    fun add(title: String, priority: Priority = Priority.MEDIUM,
            tags: List<String> = emptyList(), dueDate: LocalDate? = null): Task {
        val task = Task(UUID.randomUUID(), title, false, priority, tags, dueDate)
        add(task)
        return task
    }

    override fun remove(id: UUID): Boolean = tasks.removeIf { it.id == id }

    fun complete(id: UUID): Boolean {
        val index = tasks.indexOfFirst { it.id == id }
        if (index == -1) return false
        tasks[index] = tasks[index].copy(completed = true)
        return true
    }

    fun findById(id: UUID): Task? = tasks.find { it.id == id }

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

typealias TaskList = List<Task>

fun TaskRepository.highPriority(): TaskList =
    filterByPriority(Priority.HIGH)

inline fun <T> measure(block: () -> T): T = block()

fun main() {
    val repo = TaskRepository()
    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    repo.add("Buy groceries", Priority.HIGH,
        listOf("shopping"), LocalDate.parse("2024-12-25", fmt))
    repo.add("Write report", Priority.MEDIUM, listOf("work"))
    repo.add("Call dentist", Priority.LOW,
        listOf("health"), LocalDate.parse("2024-11-01", fmt))

    println("Overdue tasks:")
    repo.overdue().forEach { println(" - ${it.title}") }

    val s = repo.summary()
    println("Total: ${s["total"]}, Done: ${s["completed"]}, Todo: ${s["pending"]}")
}
// End of Kotlin sample
