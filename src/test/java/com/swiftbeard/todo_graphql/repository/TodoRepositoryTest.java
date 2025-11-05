package com.swiftbeard.todo_graphql.repository;

import com.swiftbeard.todo_graphql.entity.Todo;
import com.swiftbeard.todo_graphql.entity.Todo.TodoPriority;
import com.swiftbeard.todo_graphql.entity.Todo.TodoStatus;
import com.swiftbeard.todo_graphql.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("TodoRepository Integration Tests")
class TodoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TodoRepository todoRepository;

    private User user1;
    private User user2;
    private Todo pendingTodo;
    private Todo completedTodo;
    private Todo overdueTodo;
    private Todo highPriorityTodo;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
            .username("user1")
            .email("user1@example.com")
            .firstName("User")
            .lastName("One")
            .isActive(true)
            .build();

        user2 = User.builder()
            .username("user2")
            .email("user2@example.com")
            .firstName("User")
            .lastName("Two")
            .isActive(true)
            .build();

        entityManager.persist(user1);
        entityManager.persist(user2);

        pendingTodo = Todo.builder()
            .title("Pending Todo")
            .description("This is pending")
            .status(TodoStatus.PENDING)
            .priority(TodoPriority.MEDIUM)
            .dueDate(LocalDateTime.now().plusDays(7))
            .user(user1)
            .build();

        completedTodo = Todo.builder()
            .title("Completed Todo")
            .description("This is completed")
            .status(TodoStatus.COMPLETED)
            .priority(TodoPriority.LOW)
            .dueDate(LocalDateTime.now().plusDays(3))
            .completedAt(LocalDateTime.now())
            .user(user1)
            .build();

        overdueTodo = Todo.builder()
            .title("Overdue Todo")
            .description("This is overdue")
            .status(TodoStatus.PENDING)
            .priority(TodoPriority.MEDIUM)
            .dueDate(LocalDateTime.now().minusDays(2))
            .user(user1)
            .build();

        highPriorityTodo = Todo.builder()
            .title("High Priority Todo")
            .description("This is high priority")
            .status(TodoStatus.IN_PROGRESS)
            .priority(TodoPriority.HIGH)
            .dueDate(LocalDateTime.now().plusDays(1))
            .user(user1)
            .build();

        entityManager.persist(pendingTodo);
        entityManager.persist(completedTodo);
        entityManager.persist(overdueTodo);
        entityManager.persist(highPriorityTodo);
        entityManager.flush();
    }

    @Test
    @DisplayName("findByUserId - should return all todos for user")
    void findByUserId_ShouldReturnUserTodos() {
        // Act
        List<Todo> todos = todoRepository.findByUserId(user1.getId());

        // Assert
        assertThat(todos).hasSize(4);
        assertThat(todos).extracting(Todo::getUser)
            .allMatch(user -> user.getId().equals(user1.getId()));
    }

    @Test
    @DisplayName("findByUserId - should return empty list when user has no todos")
    void findByUserId_WhenNoTodos_ShouldReturnEmptyList() {
        // Act
        List<Todo> todos = todoRepository.findByUserId(user2.getId());

        // Assert
        assertThat(todos).isEmpty();
    }

    @Test
    @DisplayName("findByUserIdAndStatus - should return todos with specific status")
    void findByUserIdAndStatus_ShouldReturnFilteredTodos() {
        // Act
        List<Todo> pendingTodos = todoRepository.findByUserIdAndStatus(user1.getId(), TodoStatus.PENDING);

        // Assert
        assertThat(pendingTodos).hasSize(2); // pendingTodo and overdueTodo
        assertThat(pendingTodos).extracting(Todo::getStatus)
            .containsOnly(TodoStatus.PENDING);
    }

    @Test
    @DisplayName("findByUserIdAndStatus - should return completed todos")
    void findByUserIdAndStatus_ForCompleted_ShouldReturnCompletedTodos() {
        // Act
        List<Todo> completed = todoRepository.findByUserIdAndStatus(user1.getId(), TodoStatus.COMPLETED);

        // Assert
        assertThat(completed).hasSize(1);
        assertThat(completed.get(0).getTitle()).isEqualTo("Completed Todo");
    }

    @Test
    @DisplayName("findByUserIdAndPriority - should return todos with specific priority")
    void findByUserIdAndPriority_ShouldReturnFilteredTodos() {
        // Act
        List<Todo> highPriorityTodos = todoRepository.findByUserIdAndPriority(user1.getId(), TodoPriority.HIGH);

        // Assert
        assertThat(highPriorityTodos).hasSize(1);
        assertThat(highPriorityTodos.get(0).getTitle()).isEqualTo("High Priority Todo");
    }

    @Test
    @DisplayName("findByStatus - should return all todos with specific status")
    void findByStatus_ShouldReturnMatchingTodos() {
        // Act
        List<Todo> inProgressTodos = todoRepository.findByStatus(TodoStatus.IN_PROGRESS);

        // Assert
        assertThat(inProgressTodos).hasSize(1);
        assertThat(inProgressTodos.get(0).getStatus()).isEqualTo(TodoStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("findByUserIdOrderedByPriorityAndDueDate - should return ordered todos")
    void findByUserIdOrderedByPriorityAndDueDate_ShouldReturnOrderedList() {
        // Act
        List<Todo> orderedTodos = todoRepository.findByUserIdOrderedByPriorityAndDueDate(user1.getId());

        // Assert
        assertThat(orderedTodos).hasSize(4);
        // First should be high priority
        assertThat(orderedTodos.get(0).getPriority()).isEqualTo(TodoPriority.HIGH);
        // Verify ordering by priority (HIGH -> MEDIUM -> LOW)
        assertThat(orderedTodos.get(0).getTitle()).isEqualTo("High Priority Todo");
    }

    @Test
    @DisplayName("findOverdueTodos - should return overdue incomplete todos")
    void findOverdueTodos_ShouldReturnOverdueTodos() {
        // Act
        List<Todo> overdueTodos = todoRepository.findOverdueTodos(LocalDateTime.now());

        // Assert
        assertThat(overdueTodos).hasSize(1);
        assertThat(overdueTodos.get(0).getTitle()).isEqualTo("Overdue Todo");
        assertThat(overdueTodos.get(0).getStatus()).isNotEqualTo(TodoStatus.COMPLETED);
        assertThat(overdueTodos.get(0).getDueDate()).isBefore(LocalDateTime.now());
    }

    @Test
    @DisplayName("findOverdueTodos - should not return completed todos")
    void findOverdueTodos_ShouldNotReturnCompletedTodos() {
        // Arrange - Create an overdue but completed todo
        Todo overdueCompleted = Todo.builder()
            .title("Overdue but Completed")
            .status(TodoStatus.COMPLETED)
            .dueDate(LocalDateTime.now().minusDays(3))
            .completedAt(LocalDateTime.now())
            .user(user1)
            .build();
        entityManager.persist(overdueCompleted);
        entityManager.flush();

        // Act
        List<Todo> overdueTodos = todoRepository.findOverdueTodos(LocalDateTime.now());

        // Assert
        assertThat(overdueTodos).hasSize(1); // Only the original overdue pending todo
        assertThat(overdueTodos).noneMatch(todo -> todo.getStatus() == TodoStatus.COMPLETED);
    }

    @Test
    @DisplayName("findOverdueTodosByUserId - should return user's overdue todos")
    void findOverdueTodosByUserId_ShouldReturnUserOverdueTodos() {
        // Act
        List<Todo> overdueTodos = todoRepository.findOverdueTodosByUserId(user1.getId(), LocalDateTime.now());

        // Assert
        assertThat(overdueTodos).hasSize(1);
        assertThat(overdueTodos.get(0).getTitle()).isEqualTo("Overdue Todo");
        assertThat(overdueTodos.get(0).getUser().getId()).isEqualTo(user1.getId());
    }

    @Test
    @DisplayName("searchTodosByUser - should find todos by title")
    void searchTodosByUser_ByTitle_ShouldReturnMatchingTodos() {
        // Act
        List<Todo> results = todoRepository.searchTodosByUser(user1.getId(), "Pending");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).contains("Pending");
    }

    @Test
    @DisplayName("searchTodosByUser - should find todos by description")
    void searchTodosByUser_ByDescription_ShouldReturnMatchingTodos() {
        // Act
        List<Todo> results = todoRepository.searchTodosByUser(user1.getId(), "high priority");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDescription()).containsIgnoringCase("high priority");
    }

    @Test
    @DisplayName("searchTodosByUser - should be case insensitive")
    void searchTodosByUser_CaseInsensitive_ShouldReturnMatching() {
        // Act
        List<Todo> results = todoRepository.searchTodosByUser(user1.getId(), "OVERDUE");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Overdue Todo");
    }

    @Test
    @DisplayName("searchTodosByUser - should return empty when no matches")
    void searchTodosByUser_WhenNoMatches_ShouldReturnEmpty() {
        // Act
        List<Todo> results = todoRepository.searchTodosByUser(user1.getId(), "nonexistent");

        // Assert
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("countByUserIdAndStatus - should return count of todos")
    void countByUserIdAndStatus_ShouldReturnCorrectCount() {
        // Act
        Long pendingCount = todoRepository.countByUserIdAndStatus(user1.getId(), TodoStatus.PENDING);
        Long completedCount = todoRepository.countByUserIdAndStatus(user1.getId(), TodoStatus.COMPLETED);
        Long inProgressCount = todoRepository.countByUserIdAndStatus(user1.getId(), TodoStatus.IN_PROGRESS);

        // Assert
        assertThat(pendingCount).isEqualTo(2L);
        assertThat(completedCount).isEqualTo(1L);
        assertThat(inProgressCount).isEqualTo(1L);
    }

    @Test
    @DisplayName("countByUserIdAndStatus - should return zero when no matches")
    void countByUserIdAndStatus_WhenNoMatches_ShouldReturnZero() {
        // Act
        Long count = todoRepository.countByUserIdAndStatus(user1.getId(), TodoStatus.CANCELLED);

        // Assert
        assertThat(count).isEqualTo(0L);
    }

    @Test
    @DisplayName("findByUserIdIn - should return todos for multiple users")
    void findByUserIdIn_ShouldReturnTodosForMultipleUsers() {
        // Arrange - Add todo for user2
        Todo user2Todo = Todo.builder()
            .title("User 2 Todo")
            .status(TodoStatus.PENDING)
            .priority(TodoPriority.MEDIUM)
            .user(user2)
            .build();
        entityManager.persist(user2Todo);
        entityManager.flush();

        // Act
        List<Todo> todos = todoRepository.findByUserIdIn(Arrays.asList(user1.getId(), user2.getId()));

        // Assert
        assertThat(todos).hasSize(5); // 4 from user1 + 1 from user2
        assertThat(todos).extracting(todo -> todo.getUser().getId())
            .containsOnly(user1.getId(), user2.getId());
    }

    @Test
    @DisplayName("findByUserIdIn - should return empty when no users match")
    void findByUserIdIn_WhenNoMatches_ShouldReturnEmpty() {
        // Act
        List<Todo> todos = todoRepository.findByUserIdIn(Arrays.asList(999L, 1000L));

        // Assert
        assertThat(todos).isEmpty();
    }

    @Test
    @DisplayName("save - should save new todo and generate id")
    void save_NewTodo_ShouldGenerateId() {
        // Arrange
        Todo newTodo = Todo.builder()
            .title("New Todo")
            .description("New Description")
            .status(TodoStatus.PENDING)
            .priority(TodoPriority.MEDIUM)
            .user(user1)
            .build();

        // Act
        Todo saved = todoRepository.save(newTodo);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("New Todo");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("save - should update existing todo")
    void save_ExistingTodo_ShouldUpdate() {
        // Arrange
        Todo todo = todoRepository.findById(pendingTodo.getId()).orElseThrow();
        todo.setTitle("Updated Title");

        // Act
        Todo updated = todoRepository.save(todo);
        entityManager.flush();
        entityManager.clear();
        Todo fetched = todoRepository.findById(updated.getId()).orElseThrow();

        // Assert
        assertThat(fetched.getTitle()).isEqualTo("Updated Title");
        assertThat(fetched.getUpdatedAt()).isAfter(fetched.getCreatedAt());
    }

    @Test
    @DisplayName("delete - should delete todo")
    void delete_ExistingTodo_ShouldRemove() {
        // Arrange
        Long todoId = pendingTodo.getId();

        // Act
        todoRepository.delete(pendingTodo);
        entityManager.flush();

        // Assert
        assertThat(todoRepository.findById(todoId)).isEmpty();
    }

    @Test
    @DisplayName("findAll - should return all todos")
    void findAll_ShouldReturnAllTodos() {
        // Act
        List<Todo> todos = todoRepository.findAll();

        // Assert
        assertThat(todos).hasSize(4);
    }

    @Test
    @DisplayName("complete method - should set status and completedAt")
    void complete_ShouldSetStatusAndCompletedAt() {
        // Arrange
        Todo todo = todoRepository.findById(pendingTodo.getId()).orElseThrow();

        // Act
        todo.complete();
        Todo saved = todoRepository.save(todo);

        // Assert
        assertThat(saved.getStatus()).isEqualTo(TodoStatus.COMPLETED);
        assertThat(saved.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("priority ordering - HIGH should come before MEDIUM and LOW")
    void priorityOrdering_ShouldOrderCorrectly() {
        // Act
        List<Todo> orderedTodos = todoRepository.findByUserIdOrderedByPriorityAndDueDate(user1.getId());

        // Assert
        assertThat(orderedTodos).hasSize(4);
        // Verify priority ordering (enum ordinal ordering)
        boolean foundHigh = false;
        boolean foundMedium = false;
        for (Todo todo : orderedTodos) {
            if (todo.getPriority() == TodoPriority.HIGH) {
                foundHigh = true;
                assertThat(foundMedium).isFalse(); // HIGH should come before MEDIUM
            }
            if (todo.getPriority() == TodoPriority.MEDIUM) {
                foundMedium = true;
            }
        }
        assertThat(foundHigh).isTrue();
    }
}
