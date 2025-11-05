package com.swiftbeard.todo_graphql.service;

import com.swiftbeard.todo_graphql.dto.CreateUserInput;
import com.swiftbeard.todo_graphql.dto.UpdateUserInput;
import com.swiftbeard.todo_graphql.entity.User;
import com.swiftbeard.todo_graphql.exception.DuplicateResourceException;
import com.swiftbeard.todo_graphql.exception.ResourceNotFoundException;
import com.swiftbeard.todo_graphql.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        log.debug("Fetching user by id: {}", id);
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public User getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    public List<User> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll();
    }

    public List<User> getActiveUsers() {
        log.debug("Fetching active users");
        return userRepository.findAllActiveUsers();
    }

    public List<User> searchUsers(String search) {
        log.debug("Searching users with term: {}", search);
        return userRepository.searchUsers(search);
    }

    @Transactional
    @CachePut(value = "users", key = "#result.id")
    public User createUser(@Valid CreateUserInput input) {
        log.info("Creating new user with username: {}", input.getUsername());

        // Check for duplicates
        if (userRepository.existsByEmail(input.getEmail())) {
            throw new DuplicateResourceException("User already exists with email: " + input.getEmail());
        }
        if (userRepository.existsByUsername(input.getUsername())) {
            throw new DuplicateResourceException("User already exists with username: " + input.getUsername());
        }

        User user = User.builder()
            .username(input.getUsername())
            .email(input.getEmail())
            .firstName(input.getFirstName())
            .lastName(input.getLastName())
            .isActive(true)
            .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getId());
        return savedUser;
    }

    @Transactional
    @CachePut(value = "users", key = "#id")
    public User updateUser(Long id, @Valid UpdateUserInput input) {
        log.info("Updating user with id: {}", id);

        User user = getUserById(id);

        // Check for duplicates if email is being updated
        if (input.getEmail() != null && !input.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(input.getEmail())) {
                throw new DuplicateResourceException("User already exists with email: " + input.getEmail());
            }
            user.setEmail(input.getEmail());
        }

        // Check for duplicates if username is being updated
        if (input.getUsername() != null && !input.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(input.getUsername())) {
                throw new DuplicateResourceException("User already exists with username: " + input.getUsername());
            }
            user.setUsername(input.getUsername());
        }

        if (input.getFirstName() != null) {
            user.setFirstName(input.getFirstName());
        }
        if (input.getLastName() != null) {
            user.setLastName(input.getLastName());
        }
        if (input.getIsActive() != null) {
            user.setIsActive(input.getIsActive());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with id: {}", updatedUser.getId());
        return updatedUser;
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public boolean deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully with id: {}", id);
        return true;
    }

    public List<User> getUsersByIds(List<Long> ids) {
        log.debug("Batch fetching users by ids: {}", ids);
        return userRepository.findAllById(ids);
    }
}
