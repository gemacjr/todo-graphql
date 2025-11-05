package com.swiftbeard.todo_graphql.service;

import com.swiftbeard.todo_graphql.dto.CreateUserInput;
import com.swiftbeard.todo_graphql.dto.UpdateUserInput;
import com.swiftbeard.todo_graphql.entity.User;
import com.swiftbeard.todo_graphql.exception.DuplicateResourceException;
import com.swiftbeard.todo_graphql.exception.ResourceNotFoundException;
import com.swiftbeard.todo_graphql.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private CreateUserInput createUserInput;
    private UpdateUserInput updateUserInput;

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

        createUserInput = CreateUserInput.builder()
            .username("newuser")
            .email("new@example.com")
            .firstName("New")
            .lastName("User")
            .build();

        updateUserInput = UpdateUserInput.builder()
            .firstName("Updated")
            .lastName("Name")
            .build();
    }

    @Test
    @DisplayName("getUserById - should return user when found")
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getUserById - should throw ResourceNotFoundException when user not found")
    void getUserById_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found with id: 999");
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("getUserByEmail - should return user when found")
    void getUserByEmail_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserByEmail("test@example.com");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("getUserByEmail - should throw ResourceNotFoundException when user not found")
    void getUserByEmail_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserByEmail("notfound@example.com"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found with email: notfound@example.com");
    }

    @Test
    @DisplayName("getUserByUsername - should return user when found")
    void getUserByUsername_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserByUsername("testuser");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("getUserByUsername - should throw ResourceNotFoundException when user not found")
    void getUserByUsername_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserByUsername("notfound"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found with username: notfound");
    }

    @Test
    @DisplayName("getAllUsers - should return all users")
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        User user2 = User.builder()
            .id(2L)
            .username("user2")
            .email("user2@example.com")
            .firstName("User")
            .lastName("Two")
            .isActive(true)
            .build();
        List<User> users = Arrays.asList(testUser, user2);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).contains(testUser, user2);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getActiveUsers - should return only active users")
    void getActiveUsers_ShouldReturnActiveUsersOnly() {
        // Arrange
        List<User> activeUsers = Arrays.asList(testUser);
        when(userRepository.findAllActiveUsers()).thenReturn(activeUsers);

        // Act
        List<User> result = userService.getActiveUsers();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();
        verify(userRepository, times(1)).findAllActiveUsers();
    }

    @Test
    @DisplayName("searchUsers - should return matching users")
    void searchUsers_ShouldReturnMatchingUsers() {
        // Arrange
        List<User> searchResults = Arrays.asList(testUser);
        when(userRepository.searchUsers("test")).thenReturn(searchResults);

        // Act
        List<User> result = userService.searchUsers("test");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).contains("test");
        verify(userRepository, times(1)).searchUsers("test");
    }

    @Test
    @DisplayName("createUser - should create user successfully")
    void createUser_WhenValidInput_ShouldCreateUser() {
        // Arrange
        User newUser = User.builder()
            .id(2L)
            .username(createUserInput.getUsername())
            .email(createUserInput.getEmail())
            .firstName(createUserInput.getFirstName())
            .lastName(createUserInput.getLastName())
            .isActive(true)
            .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // Act
        User result = userService.createUser(createUserInput);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getIsActive()).isTrue();
        verify(userRepository, times(1)).existsByEmail("new@example.com");
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("createUser - should throw DuplicateResourceException when email exists")
    void createUser_WhenEmailExists_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(createUserInput))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("User already exists with email: new@example.com");
        verify(userRepository, times(1)).existsByEmail("new@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("createUser - should throw DuplicateResourceException when username exists")
    void createUser_WhenUsernameExists_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(createUserInput))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("User already exists with username: newuser");
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser - should update user successfully")
    void updateUser_WhenValidInput_ShouldUpdateUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(1L, updateUserInput);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("Updated");
        assertThat(result.getLastName()).isEqualTo("Name");
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("updateUser - should throw DuplicateResourceException when updating to existing email")
    void updateUser_WhenEmailExists_ShouldThrowException() {
        // Arrange
        UpdateUserInput inputWithEmail = UpdateUserInput.builder()
            .email("existing@example.com")
            .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(1L, inputWithEmail))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("User already exists with email: existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser - should throw DuplicateResourceException when updating to existing username")
    void updateUser_WhenUsernameExists_ShouldThrowException() {
        // Arrange
        UpdateUserInput inputWithUsername = UpdateUserInput.builder()
            .username("existinguser")
            .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(1L, inputWithUsername))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("User already exists with username: existinguser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser - should allow updating to same email")
    void updateUser_WhenSameEmail_ShouldNotThrowException() {
        // Arrange
        UpdateUserInput inputWithSameEmail = UpdateUserInput.builder()
            .email("test@example.com")
            .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(1L, inputWithSameEmail);

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("deleteUser - should delete user successfully")
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        // Act
        boolean result = userService.deleteUser(1L);

        // Assert
        assertThat(result).isTrue();
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteUser - should throw ResourceNotFoundException when user not found")
    void deleteUser_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.existsById(anyLong())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found with id: 999");
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("getUsersByIds - should return users for given ids")
    void getUsersByIds_ShouldReturnMatchingUsers() {
        // Arrange
        User user2 = User.builder()
            .id(2L)
            .username("user2")
            .email("user2@example.com")
            .firstName("User")
            .lastName("Two")
            .isActive(true)
            .build();
        List<Long> ids = Arrays.asList(1L, 2L);
        List<User> users = Arrays.asList(testUser, user2);
        when(userRepository.findAllById(ids)).thenReturn(users);

        // Act
        List<User> result = userService.getUsersByIds(ids);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(testUser, user2);
        verify(userRepository, times(1)).findAllById(ids);
    }
}
