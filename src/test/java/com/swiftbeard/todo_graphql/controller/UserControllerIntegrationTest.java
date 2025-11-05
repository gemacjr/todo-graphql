package com.swiftbeard.todo_graphql.controller;

import com.swiftbeard.todo_graphql.entity.Todo;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@GraphQlTest(UserController.class)
@DisplayName("UserController GraphQL Integration Tests")
class UserControllerIntegrationTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TodoRepository todoRepository;

    private User testUser;
    private List<Todo> testTodos;

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

        Todo todo1 = Todo.builder()
            .id(1L)
            .title("Test Todo 1")
            .status(TodoStatus.PENDING)
            .user(testUser)
            .build();

        Todo todo2 = Todo.builder()
            .id(2L)
            .title("Test Todo 2")
            .status(TodoStatus.COMPLETED)
            .completedAt(LocalDateTime.now())
            .user(testUser)
            .build();

        testTodos = Arrays.asList(todo1, todo2);
    }

    @Test
    @DisplayName("user query - should return user by id")
    void userQuery_ShouldReturnUserById() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        graphQlTester.document("""
            query {
                user(id: 1) {
                    id
                    username
                    email
                    firstName
                    lastName
                    isActive
                }
            }
            """)
            .execute()
            .path("user.id").entity(Long.class).isEqualTo(1L)
            .path("user.username").entity(String.class).isEqualTo("testuser")
            .path("user.email").entity(String.class).isEqualTo("test@example.com")
            .path("user.firstName").entity(String.class).isEqualTo("Test")
            .path("user.lastName").entity(String.class).isEqualTo("User")
            .path("user.isActive").entity(Boolean.class).isEqualTo(true);
    }

    @Test
    @DisplayName("user query - should return error when user not found")
    void userQuery_WhenUserNotFound_ShouldReturnError() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        graphQlTester.document("""
            query {
                user(id: 999) {
                    id
                    username
                }
            }
            """)
            .execute()
            .errors()
            .expect(error -> error.getMessage().contains("User not found with id: 999"));
    }

    @Test
    @DisplayName("userByEmail query - should return user by email")
    void userByEmailQuery_ShouldReturnUserByEmail() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        graphQlTester.document("""
            query {
                userByEmail(email: "test@example.com") {
                    id
                    email
                    username
                }
            }
            """)
            .execute()
            .path("userByEmail.id").entity(Long.class).isEqualTo(1L)
            .path("userByEmail.email").entity(String.class).isEqualTo("test@example.com")
            .path("userByEmail.username").entity(String.class).isEqualTo("testuser");
    }

    @Test
    @DisplayName("userByUsername query - should return user by username")
    void userByUsernameQuery_ShouldReturnUserByUsername() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        graphQlTester.document("""
            query {
                userByUsername(username: "testuser") {
                    id
                    username
                    email
                }
            }
            """)
            .execute()
            .path("userByUsername.id").entity(Long.class).isEqualTo(1L)
            .path("userByUsername.username").entity(String.class).isEqualTo("testuser")
            .path("userByUsername.email").entity(String.class).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("users query - should return all users")
    void usersQuery_ShouldReturnAllUsers() {
        // Arrange
        User user2 = User.builder()
            .id(2L)
            .username("user2")
            .email("user2@example.com")
            .firstName("User")
            .lastName("Two")
            .isActive(true)
            .build();
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        // Act & Assert
        graphQlTester.document("""
            query {
                users {
                    id
                    username
                    email
                }
            }
            """)
            .execute()
            .path("users").entityList(Object.class).hasSize(2)
            .path("users[0].id").entity(Long.class).isEqualTo(1L)
            .path("users[1].id").entity(Long.class).isEqualTo(2L);
    }

    @Test
    @DisplayName("activeUsers query - should return only active users")
    void activeUsersQuery_ShouldReturnActiveUsers() {
        // Arrange
        when(userRepository.findAllActiveUsers()).thenReturn(Arrays.asList(testUser));

        // Act & Assert
        graphQlTester.document("""
            query {
                activeUsers {
                    id
                    username
                    isActive
                }
            }
            """)
            .execute()
            .path("activeUsers").entityList(Object.class).hasSize(1)
            .path("activeUsers[0].id").entity(Long.class).isEqualTo(1L)
            .path("activeUsers[0].isActive").entity(Boolean.class).isEqualTo(true);
    }

    @Test
    @DisplayName("searchUsers query - should return matching users")
    void searchUsersQuery_ShouldReturnMatchingUsers() {
        // Arrange
        when(userRepository.searchUsers("test")).thenReturn(Arrays.asList(testUser));

        // Act & Assert
        graphQlTester.document("""
            query {
                searchUsers(search: "test") {
                    id
                    username
                    email
                }
            }
            """)
            .execute()
            .path("searchUsers").entityList(Object.class).hasSize(1)
            .path("searchUsers[0].username").entity(String.class).isEqualTo("testuser");
    }

    @Test
    @DisplayName("createUser mutation - should create new user")
    void createUserMutation_ShouldCreateUser() {
        // Arrange
        User newUser = User.builder()
            .id(2L)
            .username("newuser")
            .email("new@example.com")
            .firstName("New")
            .lastName("User")
            .isActive(true)
            .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // Act & Assert
        graphQlTester.document("""
            mutation {
                createUser(input: {
                    username: "newuser"
                    email: "new@example.com"
                    firstName: "New"
                    lastName: "User"
                }) {
                    id
                    username
                    email
                    firstName
                    lastName
                    isActive
                }
            }
            """)
            .execute()
            .path("createUser.id").entity(Long.class).isEqualTo(2L)
            .path("createUser.username").entity(String.class).isEqualTo("newuser")
            .path("createUser.email").entity(String.class).isEqualTo("new@example.com")
            .path("createUser.isActive").entity(Boolean.class).isEqualTo(true);
    }

    @Test
    @DisplayName("createUser mutation - should return error when email exists")
    void createUserMutation_WhenEmailExists_ShouldReturnError() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        graphQlTester.document("""
            mutation {
                createUser(input: {
                    username: "newuser"
                    email: "test@example.com"
                    firstName: "New"
                    lastName: "User"
                }) {
                    id
                    username
                }
            }
            """)
            .execute()
            .errors()
            .expect(error -> error.getMessage().contains("User already exists with email"));
    }

    @Test
    @DisplayName("updateUser mutation - should update user")
    void updateUserMutation_ShouldUpdateUser() {
        // Arrange
        User updatedUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .firstName("Updated")
            .lastName("Name")
            .isActive(true)
            .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act & Assert
        graphQlTester.document("""
            mutation {
                updateUser(id: 1, input: {
                    firstName: "Updated"
                    lastName: "Name"
                }) {
                    id
                    firstName
                    lastName
                }
            }
            """)
            .execute()
            .path("updateUser.id").entity(Long.class).isEqualTo(1L)
            .path("updateUser.firstName").entity(String.class).isEqualTo("Updated")
            .path("updateUser.lastName").entity(String.class).isEqualTo("Name");
    }

    @Test
    @DisplayName("deleteUser mutation - should delete user")
    void deleteUserMutation_ShouldDeleteUser() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);

        // Act & Assert
        graphQlTester.document("""
            mutation {
                deleteUser(id: 1)
            }
            """)
            .execute()
            .path("deleteUser").entity(Boolean.class).isEqualTo(true);
    }

    @Test
    @DisplayName("user.todoCount field - should return todo count")
    void userTodoCountField_ShouldReturnCount() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(todoRepository.findByUserId(1L)).thenReturn(testTodos);

        // Act & Assert
        graphQlTester.document("""
            query {
                user(id: 1) {
                    id
                    todoCount
                }
            }
            """)
            .execute()
            .path("user.id").entity(Long.class).isEqualTo(1L)
            .path("user.todoCount").entity(Integer.class).isEqualTo(2);
    }

    @Test
    @DisplayName("user.completedTodoCount field - should return completed todo count")
    void userCompletedTodoCountField_ShouldReturnCount() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(todoRepository.countByUserIdAndStatus(1L, TodoStatus.COMPLETED)).thenReturn(1L);

        // Act & Assert
        graphQlTester.document("""
            query {
                user(id: 1) {
                    id
                    completedTodoCount
                }
            }
            """)
            .execute()
            .path("user.id").entity(Long.class).isEqualTo(1L)
            .path("user.completedTodoCount").entity(Integer.class).isEqualTo(1);
    }

    @Test
    @DisplayName("user.pendingTodoCount field - should return pending todo count")
    void userPendingTodoCountField_ShouldReturnCount() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(todoRepository.countByUserIdAndStatus(1L, TodoStatus.PENDING)).thenReturn(1L);

        // Act & Assert
        graphQlTester.document("""
            query {
                user(id: 1) {
                    id
                    pendingTodoCount
                }
            }
            """)
            .execute()
            .path("user.id").entity(Long.class).isEqualTo(1L)
            .path("user.pendingTodoCount").entity(Integer.class).isEqualTo(1);
    }
}
