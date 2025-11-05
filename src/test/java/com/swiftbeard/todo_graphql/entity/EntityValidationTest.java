package com.swiftbeard.todo_graphql.entity;

import com.swiftbeard.todo_graphql.entity.Todo.TodoPriority;
import com.swiftbeard.todo_graphql.entity.Todo.TodoStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Entity Validation Tests")
class EntityValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("User - valid user should pass validation")
    void user_WhenValid_ShouldPassValidation() {
        // Arrange
        User user = User.builder()
            .username("testuser")
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .isActive(true)
            .build();

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("User - username cannot be blank")
    void user_WhenUsernameBlank_ShouldFailValidation() {
        // Arrange
        User user = User.builder()
            .username("")
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .isActive(true)
            .build();

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    @DisplayName("User - email cannot be blank")
    void user_WhenEmailBlank_ShouldFailValidation() {
        // Arrange
        User user = User.builder()
            .username("testuser")
            .email("")
            .firstName("Test")
            .lastName("User")
            .isActive(true)
            .build();

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("User - email must be valid format")
    void user_WhenEmailInvalid_ShouldFailValidation() {
        // Arrange
        User user = User.builder()
            .username("testuser")
            .email("invalid-email")
            .firstName("Test")
            .lastName("User")
            .isActive(true)
            .build();

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("User - username must be between 3 and 50 characters")
    void user_WhenUsernameTooShort_ShouldFailValidation() {
        // Arrange
        User user = User.builder()
            .username("ab")
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .isActive(true)
            .build();

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    @DisplayName("Todo - valid todo should pass validation")
    void todo_WhenValid_ShouldPassValidation() {
        // Arrange
        User user = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .build();

        Todo todo = Todo.builder()
            .title("Test Todo")
            .description("Test Description")
            .status(TodoStatus.PENDING)
            .priority(TodoPriority.MEDIUM)
            .user(user)
            .build();

        // Act
        Set<ConstraintViolation<Todo>> violations = validator.validate(todo);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Todo - title cannot be blank")
    void todo_WhenTitleBlank_ShouldFailValidation() {
        // Arrange
        User user = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .build();

        Todo todo = Todo.builder()
            .title("")
            .description("Test Description")
            .status(TodoStatus.PENDING)
            .priority(TodoPriority.MEDIUM)
            .user(user)
            .build();

        // Act
        Set<ConstraintViolation<Todo>> violations = validator.validate(todo);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("title"));
    }

    @Test
    @DisplayName("Todo - title cannot exceed 200 characters")
    void todo_WhenTitleTooLong_ShouldFailValidation() {
        // Arrange
        User user = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .build();

        String longTitle = "a".repeat(201);
        Todo todo = Todo.builder()
            .title(longTitle)
            .description("Test Description")
            .status(TodoStatus.PENDING)
            .priority(TodoPriority.MEDIUM)
            .user(user)
            .build();

        // Act
        Set<ConstraintViolation<Todo>> violations = validator.validate(todo);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("title"));
    }

    @Test
    @DisplayName("Todo - description cannot exceed 2000 characters")
    void todo_WhenDescriptionTooLong_ShouldFailValidation() {
        // Arrange
        User user = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .build();

        String longDescription = "a".repeat(2001);
        Todo todo = Todo.builder()
            .title("Test Todo")
            .description(longDescription)
            .status(TodoStatus.PENDING)
            .priority(TodoPriority.MEDIUM)
            .user(user)
            .build();

        // Act
        Set<ConstraintViolation<Todo>> violations = validator.validate(todo);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("description"));
    }

    @Test
    @DisplayName("Todo - status cannot be null")
    void todo_WhenStatusNull_ShouldFailValidation() {
        // Arrange
        User user = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .build();

        Todo todo = Todo.builder()
            .title("Test Todo")
            .description("Test Description")
            .status(null)
            .priority(TodoPriority.MEDIUM)
            .user(user)
            .build();

        // Act
        Set<ConstraintViolation<Todo>> violations = validator.validate(todo);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("status"));
    }

    @Test
    @DisplayName("Todo - priority cannot be null")
    void todo_WhenPriorityNull_ShouldFailValidation() {
        // Arrange
        User user = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .build();

        Todo todo = Todo.builder()
            .title("Test Todo")
            .description("Test Description")
            .status(TodoStatus.PENDING)
            .priority(null)
            .user(user)
            .build();

        // Act
        Set<ConstraintViolation<Todo>> violations = validator.validate(todo);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("priority"));
    }

    @Test
    @DisplayName("Todo - complete method should set status and completedAt")
    void todo_CompleteMethod_ShouldSetStatusAndTimestamp() {
        // Arrange
        User user = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .build();

        Todo todo = Todo.builder()
            .title("Test Todo")
            .status(TodoStatus.PENDING)
            .priority(TodoPriority.MEDIUM)
            .user(user)
            .build();

        // Act
        todo.complete();

        // Assert
        assertThat(todo.getStatus()).isEqualTo(TodoStatus.COMPLETED);
        assertThat(todo.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("User - helper methods should manage todos relationship")
    void user_HelperMethods_ShouldManageRelationship() {
        // Arrange
        User user = User.builder()
            .username("testuser")
            .email("test@example.com")
            .build();

        Todo todo = Todo.builder()
            .title("Test Todo")
            .status(TodoStatus.PENDING)
            .priority(TodoPriority.MEDIUM)
            .build();

        // Act
        user.addTodo(todo);

        // Assert
        assertThat(user.getTodos()).contains(todo);
        assertThat(todo.getUser()).isEqualTo(user);

        // Act - Remove
        user.removeTodo(todo);

        // Assert
        assertThat(user.getTodos()).doesNotContain(todo);
        assertThat(todo.getUser()).isNull();
    }
}
