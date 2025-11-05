package com.swiftbeard.todo_graphql.service;

import com.swiftbeard.todo_graphql.dto.CreateTodoInput;
import com.swiftbeard.todo_graphql.dto.UpdateTodoInput;
import com.swiftbeard.todo_graphql.entity.Todo;
import com.swiftbeard.todo_graphql.entity.Todo.TodoPriority;
import com.swiftbeard.todo_graphql.entity.Todo.TodoStatus;
import com.swiftbeard.todo_graphql.entity.User;
import com.swiftbeard.todo_graphql.exception.ResourceNotFoundException;
import com.swiftbeard.todo_graphql.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TodoService Unit Tests")
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TodoService todoService;

    private User testUser;
    private Todo testTodo;
    private CreateTodoInput createTodoInput;
    private UpdateTodoInput updateTodoInput;

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

        createTodoInput = CreateTodoInput.builder()
            .title("New Todo")
            .description("New Description")
            .userId(1L)
            .priority(TodoPriority.HIGH)
            .dueDate(LocalDateTime.now().plusDays(3))
            .build();

        updateTodoInput = UpdateTodoInput.builder()
            .title("Updated Todo")
            .description("Updated Description")
            .build();
    }

    @Test
    @DisplayName("getTodoById - should return todo when found")
    void getTodoById_WhenTodoExists_ShouldReturnTodo() {
        // Arrange
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));

        // Act
        Todo result = todoService.getTodoById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Todo");
        verify(todoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getTodoById - should throw ResourceNotFoundException when todo not found")
    void getTodoById_WhenTodoNotFound_ShouldThrowException() {
        // Arrange
        when(todoRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> todoService.getTodoById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Todo not found with id: 999");
        verify(todoRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("getTodosByUserId - should return todos for user")
    void getTodosByUserId_WhenUserExists_ShouldReturnTodos() {
        // Arrange
        List<Todo> todos = Arrays.asList(testTodo);
        when(userService.getUserById(1L)).thenReturn(testUser);
        when(todoRepository.findByUserId(1L)).thenReturn(todos);

        // Act
        List<Todo> result = todoService.getTodosByUserId(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Todo");
        verify(userService, times(1)).getUserById(1L);
        verify(todoRepository, times(1)).findByUserId(1L);
    }

    @Test
    @DisplayName("getTodosByUserId - should throw exception when user not found")
    void getTodosByUserId_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(userService.getUserById(anyLong())).thenThrow(new ResourceNotFoundException("User not found"));

        // Act & Assert
        assertThatThrownBy(() -> todoService.getTodosByUserId(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found");
        verify(todoRepository, never()).findByUserId(anyLong());
    }

    @Test
    @DisplayName("getTodosByUserIdAndStatus - should return todos with specific status")
    void getTodosByUserIdAndStatus_ShouldReturnFilteredTodos() {
        // Arrange
        List<Todo> pendingTodos = Arrays.asList(testTodo);
        when(todoRepository.findByUserIdAndStatus(1L, TodoStatus.PENDING)).thenReturn(pendingTodos);

        // Act
        List<Todo> result = todoService.getTodosByUserIdAndStatus(1L, TodoStatus.PENDING);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(TodoStatus.PENDING);
        verify(todoRepository, times(1)).findByUserIdAndStatus(1L, TodoStatus.PENDING);
    }

    @Test
    @DisplayName("getTodosByUserIdOrdered - should return ordered todos")
    void getTodosByUserIdOrdered_ShouldReturnOrderedTodos() {
        // Arrange
        Todo highPriorityTodo = Todo.builder()
            .id(2L)
            .title("High Priority")
            .priority(TodoPriority.HIGH)
            .user(testUser)
            .build();
        List<Todo> orderedTodos = Arrays.asList(highPriorityTodo, testTodo);
        when(todoRepository.findByUserIdOrderedByPriorityAndDueDate(1L)).thenReturn(orderedTodos);

        // Act
        List<Todo> result = todoService.getTodosByUserIdOrdered(1L);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPriority()).isEqualTo(TodoPriority.HIGH);
        verify(todoRepository, times(1)).findByUserIdOrderedByPriorityAndDueDate(1L);
    }

    @Test
    @DisplayName("getOverdueTodos - should return overdue todos")
    void getOverdueTodos_ShouldReturnOverdueTodos() {
        // Arrange
        Todo overdueTodo = Todo.builder()
            .id(2L)
            .title("Overdue Todo")
            .dueDate(LocalDateTime.now().minusDays(1))
            .status(TodoStatus.PENDING)
            .user(testUser)
            .build();
        List<Todo> overdueTodos = Arrays.asList(overdueTodo);
        when(todoRepository.findOverdueTodos(any(LocalDateTime.class))).thenReturn(overdueTodos);

        // Act
        List<Todo> result = todoService.getOverdueTodos();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Overdue Todo");
        verify(todoRepository, times(1)).findOverdueTodos(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("getOverdueTodosByUserId - should return overdue todos for user")
    void getOverdueTodosByUserId_ShouldReturnUserOverdueTodos() {
        // Arrange
        Todo overdueTodo = Todo.builder()
            .id(2L)
            .title("Overdue Todo")
            .dueDate(LocalDateTime.now().minusDays(1))
            .status(TodoStatus.PENDING)
            .user(testUser)
            .build();
        List<Todo> overdueTodos = Arrays.asList(overdueTodo);
        when(todoRepository.findOverdueTodosByUserId(eq(1L), any(LocalDateTime.class))).thenReturn(overdueTodos);

        // Act
        List<Todo> result = todoService.getOverdueTodosByUserId(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getId()).isEqualTo(1L);
        verify(todoRepository, times(1)).findOverdueTodosByUserId(eq(1L), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("getAllTodos - should return all todos")
    void getAllTodos_ShouldReturnAllTodos() {
        // Arrange
        Todo todo2 = Todo.builder()
            .id(2L)
            .title("Another Todo")
            .user(testUser)
            .build();
        List<Todo> todos = Arrays.asList(testTodo, todo2);
        when(todoRepository.findAll()).thenReturn(todos);

        // Act
        List<Todo> result = todoService.getAllTodos();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).contains(testTodo, todo2);
        verify(todoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("searchTodosByUser - should return matching todos")
    void searchTodosByUser_ShouldReturnMatchingTodos() {
        // Arrange
        List<Todo> searchResults = Arrays.asList(testTodo);
        when(todoRepository.searchTodosByUser(1L, "test")).thenReturn(searchResults);

        // Act
        List<Todo> result = todoService.searchTodosByUser(1L, "test");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).containsIgnoringCase("test");
        verify(todoRepository, times(1)).searchTodosByUser(1L, "test");
    }

    @Test
    @DisplayName("createTodo - should create todo successfully")
    void createTodo_WhenValidInput_ShouldCreateTodo() {
        // Arrange
        Todo newTodo = Todo.builder()
            .id(2L)
            .title(createTodoInput.getTitle())
            .description(createTodoInput.getDescription())
            .status(TodoStatus.PENDING)
            .priority(createTodoInput.getPriority())
            .dueDate(createTodoInput.getDueDate())
            .user(testUser)
            .build();

        when(userService.getUserById(1L)).thenReturn(testUser);
        when(todoRepository.save(any(Todo.class))).thenReturn(newTodo);

        // Act
        Todo result = todoService.createTodo(createTodoInput);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTitle()).isEqualTo("New Todo");
        assertThat(result.getPriority()).isEqualTo(TodoPriority.HIGH);
        verify(userService, times(1)).getUserById(1L);
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    @DisplayName("createTodo - should throw exception when user not found")
    void createTodo_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(userService.getUserById(anyLong())).thenThrow(new ResourceNotFoundException("User not found"));

        // Act & Assert
        assertThatThrownBy(() -> todoService.createTodo(createTodoInput))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found");
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    @DisplayName("createTodo - should set default status to PENDING when not specified")
    void createTodo_WhenStatusNotSpecified_ShouldSetDefaultStatus() {
        // Arrange
        CreateTodoInput inputWithoutStatus = CreateTodoInput.builder()
            .title("New Todo")
            .userId(1L)
            .build();
        Todo savedTodo = Todo.builder()
            .id(2L)
            .title("New Todo")
            .status(TodoStatus.PENDING)
            .user(testUser)
            .build();

        when(userService.getUserById(1L)).thenReturn(testUser);
        when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);

        // Act
        Todo result = todoService.createTodo(inputWithoutStatus);

        // Assert
        assertThat(result.getStatus()).isEqualTo(TodoStatus.PENDING);
    }

    @Test
    @DisplayName("updateTodo - should update todo successfully")
    void updateTodo_WhenValidInput_ShouldUpdateTodo() {
        // Arrange
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);

        // Act
        Todo result = todoService.updateTodo(1L, updateTodoInput);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Updated Todo");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
        verify(todoRepository, times(1)).findById(1L);
        verify(todoRepository, times(1)).save(testTodo);
    }

    @Test
    @DisplayName("updateTodo - should set completedAt when status changes to COMPLETED")
    void updateTodo_WhenStatusChangedToCompleted_ShouldSetCompletedAt() {
        // Arrange
        UpdateTodoInput inputWithStatus = UpdateTodoInput.builder()
            .status(TodoStatus.COMPLETED)
            .build();
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Todo result = todoService.updateTodo(1L, inputWithStatus);

        // Assert
        assertThat(result.getStatus()).isEqualTo(TodoStatus.COMPLETED);
        assertThat(result.getCompletedAt()).isNotNull();
        verify(todoRepository, times(1)).save(testTodo);
    }

    @Test
    @DisplayName("updateTodo - should not update completedAt if already set")
    void updateTodo_WhenCompletedAtAlreadySet_ShouldNotUpdateCompletedAt() {
        // Arrange
        LocalDateTime originalCompletedAt = LocalDateTime.now().minusDays(1);
        testTodo.setStatus(TodoStatus.COMPLETED);
        testTodo.setCompletedAt(originalCompletedAt);
        UpdateTodoInput inputWithStatus = UpdateTodoInput.builder()
            .status(TodoStatus.COMPLETED)
            .build();
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Todo result = todoService.updateTodo(1L, inputWithStatus);

        // Assert
        assertThat(result.getCompletedAt()).isEqualTo(originalCompletedAt);
    }

    @Test
    @DisplayName("deleteTodo - should delete todo successfully")
    void deleteTodo_WhenTodoExists_ShouldDeleteTodo() {
        // Arrange
        when(todoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(todoRepository).deleteById(1L);

        // Act
        boolean result = todoService.deleteTodo(1L);

        // Assert
        assertThat(result).isTrue();
        verify(todoRepository, times(1)).existsById(1L);
        verify(todoRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteTodo - should throw ResourceNotFoundException when todo not found")
    void deleteTodo_WhenTodoNotFound_ShouldThrowException() {
        // Arrange
        when(todoRepository.existsById(anyLong())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> todoService.deleteTodo(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Todo not found with id: 999");
        verify(todoRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("completeTodo - should complete todo successfully")
    void completeTodo_WhenTodoExists_ShouldCompleteTodo() {
        // Arrange
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Todo result = todoService.completeTodo(1L);

        // Assert
        assertThat(result.getStatus()).isEqualTo(TodoStatus.COMPLETED);
        assertThat(result.getCompletedAt()).isNotNull();
        verify(todoRepository, times(1)).save(testTodo);
    }

    @Test
    @DisplayName("getTodosByUserIds - should return todos grouped by user id")
    void getTodosByUserIds_ShouldReturnGroupedTodos() {
        // Arrange
        User user2 = User.builder()
            .id(2L)
            .username("user2")
            .email("user2@example.com")
            .build();
        Todo todo2 = Todo.builder()
            .id(2L)
            .title("Todo 2")
            .user(user2)
            .build();
        List<Long> userIds = Arrays.asList(1L, 2L);
        List<Todo> todos = Arrays.asList(testTodo, todo2);
        when(todoRepository.findByUserIdIn(userIds)).thenReturn(todos);

        // Act
        Map<Long, List<Todo>> result = todoService.getTodosByUserIds(userIds);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(1L)).hasSize(1);
        assertThat(result.get(2L)).hasSize(1);
        assertThat(result.get(1L).get(0).getTitle()).isEqualTo("Test Todo");
        assertThat(result.get(2L).get(0).getTitle()).isEqualTo("Todo 2");
        verify(todoRepository, times(1)).findByUserIdIn(userIds);
    }

    @Test
    @DisplayName("countTodosByUserIdAndStatus - should return count")
    void countTodosByUserIdAndStatus_ShouldReturnCount() {
        // Arrange
        when(todoRepository.countByUserIdAndStatus(1L, TodoStatus.PENDING)).thenReturn(5L);

        // Act
        Long result = todoService.countTodosByUserIdAndStatus(1L, TodoStatus.PENDING);

        // Assert
        assertThat(result).isEqualTo(5L);
        verify(todoRepository, times(1)).countByUserIdAndStatus(1L, TodoStatus.PENDING);
    }
}
