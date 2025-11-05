package com.swiftbeard.todo_graphql.controller;

import com.swiftbeard.todo_graphql.entity.Todo;
import com.swiftbeard.todo_graphql.entity.Todo.TodoPriority;
import com.swiftbeard.todo_graphql.entity.Todo.TodoStatus;
import com.swiftbeard.todo_graphql.entity.User;
import com.swiftbeard.todo_graphql.repository.TodoRepository;
import com.swiftbeard.todo_graphql.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@GraphQlTest(TodoController.class)
@DisplayName("TodoController GraphQL Integration Tests")
class TodoControllerIntegrationTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockBean
    private TodoRepository todoRepository;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private Todo testTodo;
    private Todo overdueTodo;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .isActive(true)
            .build();

        testTodo = Todo.builder()
            .id(1L)
            .title("Test Todo")
            .description("Test Description")
            .status(TodoStatus.PENDING)
            .priority(TodoPriority.MEDIUM)
            .dueDate(LocalDateTime.now().plusDays(7))
            .user(testUser)
            .build();

        overdueTodo = Todo.builder()
            .id(2L)
            .title("Overdue Todo")
            .description("This is overdue")
            .status(TodoStatus.PENDING)
            .priority(TodoPriority.HIGH)
            .dueDate(LocalDateTime.now().minusDays(1))
            .user(testUser)
            .build();
    }

    @Test
    @DisplayName("todo query - should return todo by id")
    void todoQuery_ShouldReturnTodoById() {
        // Arrange
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));

        // Act & Assert
        graphQlTester.document("""
            query {
                todo(id: 1) {
                    id
                    title
                    description
                    status
                    priority
                }
            }
            """)
            .execute()
            .path("todo.id").entity(Long.class).isEqualTo(1L)
            .path("todo.title").entity(String.class).isEqualTo("Test Todo")
            .path("todo.description").entity(String.class).isEqualTo("Test Description")
            .path("todo.status").entity(String.class).isEqualTo("PENDING")
            .path("todo.priority").entity(String.class).isEqualTo("MEDIUM");
    }

    @Test
    @DisplayName("todo query - should return error when todo not found")
    void todoQuery_WhenTodoNotFound_ShouldReturnError() {
        // Arrange
        when(todoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        graphQlTester.document("""
            query {
                todo(id: 999) {
                    id
                    title
                }
            }
            """)
            .execute()
            .errors()
            .expect(error -> error.getMessage().contains("Todo not found with id: 999"));
    }

    @Test
    @DisplayName("todos query - should return all todos")
    void todosQuery_ShouldReturnAllTodos() {
        // Arrange
        when(todoRepository.findAll()).thenReturn(Arrays.asList(testTodo, overdueTodo));

        // Act & Assert
        graphQlTester.document("""
            query {
                todos {
                    id
                    title
                    status
                }
            }
            """)
            .execute()
            .path("todos").entityList(Object.class).hasSize(2)
            .path("todos[0].id").entity(Long.class).isEqualTo(1L)
            .path("todos[1].id").entity(Long.class).isEqualTo(2L);
    }

    @Test
    @DisplayName("todosByUser query - should return todos for user")
    void todosByUserQuery_ShouldReturnUserTodos() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(todoRepository.findByUserId(1L)).thenReturn(Arrays.asList(testTodo, overdueTodo));

        // Act & Assert
        graphQlTester.document("""
            query {
                todosByUser(userId: 1) {
                    id
                    title
                }
            }
            """)
            .execute()
            .path("todosByUser").entityList(Object.class).hasSize(2)
            .path("todosByUser[0].title").entity(String.class).isEqualTo("Test Todo");
    }

    @Test
    @DisplayName("todosByUserAndStatus query - should return filtered todos")
    void todosByUserAndStatusQuery_ShouldReturnFilteredTodos() {
        // Arrange
        when(todoRepository.findByUserIdAndStatus(1L, TodoStatus.PENDING))
            .thenReturn(Arrays.asList(testTodo, overdueTodo));

        // Act & Assert
        graphQlTester.document("""
            query {
                todosByUserAndStatus(userId: 1, status: PENDING) {
                    id
                    title
                    status
                }
            }
            """)
            .execute()
            .path("todosByUserAndStatus").entityList(Object.class).hasSize(2)
            .path("todosByUserAndStatus[0].status").entity(String.class).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("todosByUserOrdered query - should return ordered todos")
    void todosByUserOrderedQuery_ShouldReturnOrderedTodos() {
        // Arrange
        when(todoRepository.findByUserIdOrderedByPriorityAndDueDate(1L))
            .thenReturn(Arrays.asList(overdueTodo, testTodo)); // High priority first

        // Act & Assert
        graphQlTester.document("""
            query {
                todosByUserOrdered(userId: 1) {
                    id
                    title
                    priority
                }
            }
            """)
            .execute()
            .path("todosByUserOrdered").entityList(Object.class).hasSize(2)
            .path("todosByUserOrdered[0].priority").entity(String.class).isEqualTo("HIGH")
            .path("todosByUserOrdered[1].priority").entity(String.class).isEqualTo("MEDIUM");
    }

    @Test
    @DisplayName("overdueTodos query - should return overdue todos")
    void overdueTodosQuery_ShouldReturnOverdueTodos() {
        // Arrange
        when(todoRepository.findOverdueTodos(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(overdueTodo));

        // Act & Assert
        graphQlTester.document("""
            query {
                overdueTodos {
                    id
                    title
                    status
                }
            }
            """)
            .execute()
            .path("overdueTodos").entityList(Object.class).hasSize(1)
            .path("overdueTodos[0].title").entity(String.class).isEqualTo("Overdue Todo");
    }

    @Test
    @DisplayName("overdueTodosByUser query - should return user's overdue todos")
    void overdueTodosByUserQuery_ShouldReturnUserOverdueTodos() {
        // Arrange
        when(todoRepository.findOverdueTodosByUserId(any(Long.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(overdueTodo));

        // Act & Assert
        graphQlTester.document("""
            query {
                overdueTodosByUser(userId: 1) {
                    id
                    title
                }
            }
            """)
            .execute()
            .path("overdueTodosByUser").entityList(Object.class).hasSize(1)
            .path("overdueTodosByUser[0].id").entity(Long.class).isEqualTo(2L);
    }

    @Test
    @DisplayName("searchTodosByUser query - should return matching todos")
    void searchTodosByUserQuery_ShouldReturnMatchingTodos() {
        // Arrange
        when(todoRepository.searchTodosByUser(1L, "Test"))
            .thenReturn(Arrays.asList(testTodo));

        // Act & Assert
        graphQlTester.document("""
            query {
                searchTodosByUser(userId: 1, search: "Test") {
                    id
                    title
                }
            }
            """)
            .execute()
            .path("searchTodosByUser").entityList(Object.class).hasSize(1)
            .path("searchTodosByUser[0].title").entity(String.class).isEqualTo("Test Todo");
    }

    @Test
    @DisplayName("todoStats query - should return statistics")
    void todoStatsQuery_ShouldReturnStatistics() {
        // Arrange
        Todo completedTodo = Todo.builder()
            .id(3L)
            .title("Completed Todo")
            .status(TodoStatus.COMPLETED)
            .completedAt(LocalDateTime.now())
            .user(testUser)
            .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(todoRepository.findByUserId(1L))
            .thenReturn(Arrays.asList(testTodo, overdueTodo, completedTodo));

        // Act & Assert
        graphQlTester.document("""
            query {
                todoStats(userId: 1) {
                    totalTodos
                    completedTodos
                    pendingTodos
                    inProgressTodos
                    cancelledTodos
                    overdueTodos
                }
            }
            """)
            .execute()
            .path("todoStats.totalTodos").entity(Integer.class).isEqualTo(3)
            .path("todoStats.completedTodos").entity(Integer.class).isEqualTo(1)
            .path("todoStats.pendingTodos").entity(Integer.class).isEqualTo(2)
            .path("todoStats.inProgressTodos").entity(Integer.class).isEqualTo(0)
            .path("todoStats.cancelledTodos").entity(Integer.class).isEqualTo(0)
            .path("todoStats.overdueTodos").entity(Integer.class).isEqualTo(1);
    }

    @Test
    @DisplayName("createTodo mutation - should create new todo")
    void createTodoMutation_ShouldCreateTodo() {
        // Arrange
        Todo newTodo = Todo.builder()
            .id(3L)
            .title("New Todo")
            .description("New Description")
            .status(TodoStatus.PENDING)
            .priority(TodoPriority.HIGH)
            .user(testUser)
            .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(todoRepository.save(any(Todo.class))).thenReturn(newTodo);

        // Act & Assert
        graphQlTester.document("""
            mutation {
                createTodo(input: {
                    title: "New Todo"
                    description: "New Description"
                    userId: 1
                    priority: HIGH
                    status: PENDING
                }) {
                    id
                    title
                    description
                    status
                    priority
                }
            }
            """)
            .execute()
            .path("createTodo.id").entity(Long.class).isEqualTo(3L)
            .path("createTodo.title").entity(String.class).isEqualTo("New Todo")
            .path("createTodo.description").entity(String.class).isEqualTo("New Description")
            .path("createTodo.status").entity(String.class).isEqualTo("PENDING")
            .path("createTodo.priority").entity(String.class).isEqualTo("HIGH");
    }

    @Test
    @DisplayName("updateTodo mutation - should update todo")
    void updateTodoMutation_ShouldUpdateTodo() {
        // Arrange
        Todo updatedTodo = Todo.builder()
            .id(1L)
            .title("Updated Todo")
            .description("Updated Description")
            .status(TodoStatus.IN_PROGRESS)
            .priority(TodoPriority.HIGH)
            .user(testUser)
            .build();

        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(updatedTodo);

        // Act & Assert
        graphQlTester.document("""
            mutation {
                updateTodo(id: 1, input: {
                    title: "Updated Todo"
                    description: "Updated Description"
                    status: IN_PROGRESS
                    priority: HIGH
                }) {
                    id
                    title
                    description
                    status
                    priority
                }
            }
            """)
            .execute()
            .path("updateTodo.id").entity(Long.class).isEqualTo(1L)
            .path("updateTodo.title").entity(String.class).isEqualTo("Updated Todo")
            .path("updateTodo.status").entity(String.class).isEqualTo("IN_PROGRESS")
            .path("updateTodo.priority").entity(String.class).isEqualTo("HIGH");
    }

    @Test
    @DisplayName("deleteTodo mutation - should delete todo")
    void deleteTodoMutation_ShouldDeleteTodo() {
        // Arrange
        when(todoRepository.existsById(1L)).thenReturn(true);

        // Act & Assert
        graphQlTester.document("""
            mutation {
                deleteTodo(id: 1)
            }
            """)
            .execute()
            .path("deleteTodo").entity(Boolean.class).isEqualTo(true);
    }

    @Test
    @DisplayName("completeTodo mutation - should complete todo")
    void completeTodoMutation_ShouldCompleteTodo() {
        // Arrange
        testTodo.complete(); // Sets status to COMPLETED and completedAt
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);

        // Act & Assert
        graphQlTester.document("""
            mutation {
                completeTodo(id: 1) {
                    id
                    status
                }
            }
            """)
            .execute()
            .path("completeTodo.id").entity(Long.class).isEqualTo(1L)
            .path("completeTodo.status").entity(String.class).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("todo.user field - should return user")
    void todoUserField_ShouldReturnUser() {
        // Arrange
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));

        // Act & Assert
        graphQlTester.document("""
            query {
                todo(id: 1) {
                    id
                    title
                    user {
                        id
                        username
                        email
                    }
                }
            }
            """)
            .execute()
            .path("todo.id").entity(Long.class).isEqualTo(1L)
            .path("todo.user.id").entity(Long.class).isEqualTo(1L)
            .path("todo.user.username").entity(String.class).isEqualTo("testuser")
            .path("todo.user.email").entity(String.class).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("todo.isOverdue field - should return true for overdue todo")
    void todoIsOverdueField_WhenOverdue_ShouldReturnTrue() {
        // Arrange
        when(todoRepository.findById(2L)).thenReturn(Optional.of(overdueTodo));

        // Act & Assert
        graphQlTester.document("""
            query {
                todo(id: 2) {
                    id
                    title
                    isOverdue
                }
            }
            """)
            .execute()
            .path("todo.id").entity(Long.class).isEqualTo(2L)
            .path("todo.isOverdue").entity(Boolean.class).isEqualTo(true);
    }

    @Test
    @DisplayName("todo.isOverdue field - should return false for non-overdue todo")
    void todoIsOverdueField_WhenNotOverdue_ShouldReturnFalse() {
        // Arrange
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));

        // Act & Assert
        graphQlTester.document("""
            query {
                todo(id: 1) {
                    id
                    title
                    isOverdue
                }
            }
            """)
            .execute()
            .path("todo.id").entity(Long.class).isEqualTo(1L)
            .path("todo.isOverdue").entity(Boolean.class).isEqualTo(false);
    }
}
