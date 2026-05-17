require 'date'
require 'securerandom'

module Todo
  class Priority
    LOW = :low
    MEDIUM = :medium
    HIGH = :high
  end

  module Validatable
    def valid_title?(title)
      !title.nil? && !title.strip.empty?
    end
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
    include Validatable

    def initialize
      @tasks = []
    end

    def add(title, priority = Priority::MEDIUM, tags = [], due_date = nil)
      raise ArgumentError, "Title must not be blank" unless valid_title?(title)
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
# End of sample
