package com.swiftbeard.todo_graphql.repository;

import com.swiftbeard.todo_graphql.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserRepository Integration Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private User inactiveUser;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
            .username("john_doe")
            .email("john@example.com")
            .firstName("John")
            .lastName("Doe")
            .isActive(true)
            .build();

        user2 = User.builder()
            .username("jane_smith")
            .email("jane@example.com")
            .firstName("Jane")
            .lastName("Smith")
            .isActive(true)
            .build();

        inactiveUser = User.builder()
            .username("inactive_user")
            .email("inactive@example.com")
            .firstName("Inactive")
            .lastName("User")
            .isActive(false)
            .build();

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(inactiveUser);
        entityManager.flush();
    }

    @Test
    @DisplayName("findByEmail - should find user by email")
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        // Act
        Optional<User> result = userRepository.findByEmail("john@example.com");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john@example.com");
        assertThat(result.get().getUsername()).isEqualTo("john_doe");
    }

    @Test
    @DisplayName("findByEmail - should return empty when user not found")
    void findByEmail_WhenUserNotFound_ShouldReturnEmpty() {
        // Act
        Optional<User> result = userRepository.findByEmail("notfound@example.com");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUsername - should find user by username")
    void findByUsername_WhenUserExists_ShouldReturnUser() {
        // Act
        Optional<User> result = userRepository.findByUsername("jane_smith");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("jane_smith");
        assertThat(result.get().getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    @DisplayName("findByUsername - should return empty when user not found")
    void findByUsername_WhenUserNotFound_ShouldReturnEmpty() {
        // Act
        Optional<User> result = userRepository.findByUsername("notfound");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail - should return true when email exists")
    void existsByEmail_WhenEmailExists_ShouldReturnTrue() {
        // Act
        boolean exists = userRepository.existsByEmail("john@example.com");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByEmail - should return false when email does not exist")
    void existsByEmail_WhenEmailNotFound_ShouldReturnFalse() {
        // Act
        boolean exists = userRepository.existsByEmail("notfound@example.com");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByUsername - should return true when username exists")
    void existsByUsername_WhenUsernameExists_ShouldReturnTrue() {
        // Act
        boolean exists = userRepository.existsByUsername("john_doe");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByUsername - should return false when username does not exist")
    void existsByUsername_WhenUsernameNotFound_ShouldReturnFalse() {
        // Act
        boolean exists = userRepository.existsByUsername("notfound");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("findByIsActive - should return only active users")
    void findByIsActive_WhenTrue_ShouldReturnActiveUsers() {
        // Act
        List<User> activeUsers = userRepository.findByIsActive(true);

        // Assert
        assertThat(activeUsers).hasSize(2);
        assertThat(activeUsers).extracting(User::getIsActive)
            .containsOnly(true);
        assertThat(activeUsers).extracting(User::getUsername)
            .containsExactlyInAnyOrder("john_doe", "jane_smith");
    }

    @Test
    @DisplayName("findByIsActive - should return only inactive users")
    void findByIsActive_WhenFalse_ShouldReturnInactiveUsers() {
        // Act
        List<User> inactiveUsers = userRepository.findByIsActive(false);

        // Assert
        assertThat(inactiveUsers).hasSize(1);
        assertThat(inactiveUsers.get(0).getIsActive()).isFalse();
        assertThat(inactiveUsers.get(0).getUsername()).isEqualTo("inactive_user");
    }

    @Test
    @DisplayName("findAllActiveUsers - should return all active users ordered by createdAt")
    void findAllActiveUsers_ShouldReturnActiveUsersOrdered() {
        // Act
        List<User> activeUsers = userRepository.findAllActiveUsers();

        // Assert
        assertThat(activeUsers).hasSize(2);
        assertThat(activeUsers).extracting(User::getIsActive)
            .containsOnly(true);
        // Verify they are ordered by createdAt DESC
        assertThat(activeUsers.get(0).getCreatedAt())
            .isAfterOrEqualTo(activeUsers.get(1).getCreatedAt());
    }

    @Test
    @DisplayName("searchUsers - should find users by username")
    void searchUsers_ByUsername_ShouldReturnMatchingUsers() {
        // Act
        List<User> results = userRepository.searchUsers("john");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUsername()).isEqualTo("john_doe");
    }

    @Test
    @DisplayName("searchUsers - should find users by email")
    void searchUsers_ByEmail_ShouldReturnMatchingUsers() {
        // Act
        List<User> results = userRepository.searchUsers("jane@");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    @DisplayName("searchUsers - should find users by first name")
    void searchUsers_ByFirstName_ShouldReturnMatchingUsers() {
        // Act
        List<User> results = userRepository.searchUsers("Jane");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFirstName()).isEqualTo("Jane");
    }

    @Test
    @DisplayName("searchUsers - should find users by last name")
    void searchUsers_ByLastName_ShouldReturnMatchingUsers() {
        // Act
        List<User> results = userRepository.searchUsers("Doe");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getLastName()).isEqualTo("Doe");
    }

    @Test
    @DisplayName("searchUsers - should be case insensitive")
    void searchUsers_CaseInsensitive_ShouldReturnMatchingUsers() {
        // Act
        List<User> results = userRepository.searchUsers("JOHN");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUsername()).isEqualTo("john_doe");
    }

    @Test
    @DisplayName("searchUsers - should return empty list when no matches")
    void searchUsers_WhenNoMatches_ShouldReturnEmptyList() {
        // Act
        List<User> results = userRepository.searchUsers("nonexistent");

        // Assert
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("searchUsers - should find multiple users with partial match")
    void searchUsers_WithPartialMatch_ShouldReturnMultipleUsers() {
        // Act
        List<User> results = userRepository.searchUsers("example.com");

        // Assert
        assertThat(results).hasSize(3);
        assertThat(results).extracting(User::getEmail)
            .allMatch(email -> email.contains("example.com"));
    }

    @Test
    @DisplayName("save - should save new user and generate id")
    void save_NewUser_ShouldGenerateId() {
        // Arrange
        User newUser = User.builder()
            .username("newuser")
            .email("new@example.com")
            .firstName("New")
            .lastName("User")
            .isActive(true)
            .build();

        // Act
        User saved = userRepository.save(newUser);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("newuser");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("save - should update existing user")
    void save_ExistingUser_ShouldUpdate() {
        // Arrange
        User user = userRepository.findByUsername("john_doe").orElseThrow();
        user.setFirstName("Updated");

        // Act
        User updated = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();
        User fetched = userRepository.findById(updated.getId()).orElseThrow();

        // Assert
        assertThat(fetched.getFirstName()).isEqualTo("Updated");
        assertThat(fetched.getUpdatedAt()).isAfter(fetched.getCreatedAt());
    }

    @Test
    @DisplayName("delete - should delete user")
    void delete_ExistingUser_ShouldRemove() {
        // Arrange
        User user = userRepository.findByUsername("john_doe").orElseThrow();
        Long userId = user.getId();

        // Act
        userRepository.delete(user);
        entityManager.flush();
        Optional<User> deleted = userRepository.findById(userId);

        // Assert
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("findAll - should return all users")
    void findAll_ShouldReturnAllUsers() {
        // Act
        List<User> users = userRepository.findAll();

        // Assert
        assertThat(users).hasSize(3);
    }

    @Test
    @DisplayName("findById - should return user by id")
    void findById_WhenUserExists_ShouldReturnUser() {
        // Arrange
        User user = userRepository.findByUsername("john_doe").orElseThrow();

        // Act
        Optional<User> result = userRepository.findById(user.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("john_doe");
    }

    @Test
    @DisplayName("findAllById - should return users for given ids")
    void findAllById_ShouldReturnMatchingUsers() {
        // Arrange
        User user1Found = userRepository.findByUsername("john_doe").orElseThrow();
        User user2Found = userRepository.findByUsername("jane_smith").orElseThrow();
        List<Long> ids = Arrays.asList(user1Found.getId(), user2Found.getId());

        // Act
        List<User> result = userRepository.findAllById(ids);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(User::getUsername)
            .containsExactlyInAnyOrder("john_doe", "jane_smith");
    }
}
