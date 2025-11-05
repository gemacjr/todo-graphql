package com.swiftbeard.todo_graphql.service;

import com.swiftbeard.todo_graphql.dto.CreateTodoInput;
import com.swiftbeard.todo_graphql.dto.UpdateTodoInput;
import com.swiftbeard.todo_graphql.entity.Todo;
import com.swiftbeard.todo_graphql.entity.Todo.TodoStatus;
import com.swiftbeard.todo_graphql.entity.User;
import com.swiftbeard.todo_graphql.exception.ResourceNotFoundException;
import com.swiftbeard.todo_graphql.repository.TodoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserService userService;

    @Cacheable(value = "todos", key = "#id")
    public Todo getTodoById(Long id) {
        log.debug("Fetching todo by id: {}", id);
        return todoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + id));
    }

    @Cacheable(value = "userTodos", key = "#userId")
    public List<Todo> getTodosByUserId(Long userId) {
        log.debug("Fetching todos for user id: {}", userId);
        // Verify user exists
        userService.getUserById(userId);
        return todoRepository.findByUserId(userId);
    }

    public List<Todo> getTodosByUserIdAndStatus(Long userId, TodoStatus status) {
        log.debug("Fetching todos for user id: {} with status: {}", userId, status);
        return todoRepository.findByUserIdAndStatus(userId, status);
    }

    public List<Todo> getTodosByUserIdOrdered(Long userId) {
        log.debug("Fetching ordered todos for user id: {}", userId);
        return todoRepository.findByUserIdOrderedByPriorityAndDueDate(userId);
    }

    public List<Todo> getOverdueTodos() {
        log.debug("Fetching overdue todos");
        return todoRepository.findOverdueTodos(LocalDateTime.now());
    }

    public List<Todo> getOverdueTodosByUserId(Long userId) {
        log.debug("Fetching overdue todos for user id: {}", userId);
        return todoRepository.findOverdueTodosByUserId(userId, LocalDateTime.now());
    }

    public List<Todo> getAllTodos() {
        log.debug("Fetching all todos");
        return todoRepository.findAll();
    }

    public List<Todo> searchTodosByUser(Long userId, String search) {
        log.debug("Searching todos for user id: {} with term: {}", userId, search);
        return todoRepository.searchTodosByUser(userId, search);
    }

    @Transactional
    @CachePut(value = "todos", key = "#result.id")
    @CacheEvict(value = "userTodos", key = "#input.userId")
    public Todo createTodo(@Valid CreateTodoInput input) {
        log.info("Creating new todo with title: {} for user id: {}", input.getTitle(), input.getUserId());

        // Verify user exists
        User user = userService.getUserById(input.getUserId());

        Todo todo = Todo.builder()
            .title(input.getTitle())
            .description(input.getDescription())
            .status(input.getStatus() != null ? input.getStatus() : TodoStatus.PENDING)
            .priority(input.getPriority())
            .dueDate(input.getDueDate())
            .user(user)
            .build();

        Todo savedTodo = todoRepository.save(todo);
        log.info("Todo created successfully with id: {}", savedTodo.getId());
        return savedTodo;
    }

    @Transactional
    @CachePut(value = "todos", key = "#id")
    @CacheEvict(value = "userTodos", allEntries = true)
    public Todo updateTodo(Long id, @Valid UpdateTodoInput input) {
        log.info("Updating todo with id: {}", id);

        Todo todo = getTodoById(id);

        if (input.getTitle() != null) {
            todo.setTitle(input.getTitle());
        }
        if (input.getDescription() != null) {
            todo.setDescription(input.getDescription());
        }
        if (input.getStatus() != null) {
            todo.setStatus(input.getStatus());
            // Automatically set completedAt when status changes to COMPLETED
            if (input.getStatus() == TodoStatus.COMPLETED && todo.getCompletedAt() == null) {
                todo.setCompletedAt(LocalDateTime.now());
            }
        }
        if (input.getPriority() != null) {
            todo.setPriority(input.getPriority());
        }
        if (input.getDueDate() != null) {
            todo.setDueDate(input.getDueDate());
        }

        Todo updatedTodo = todoRepository.save(todo);
        log.info("Todo updated successfully with id: {}", updatedTodo.getId());
        return updatedTodo;
    }

    @Transactional
    @CacheEvict(value = {"todos", "userTodos"}, allEntries = true)
    public boolean deleteTodo(Long id) {
        log.info("Deleting todo with id: {}", id);

        if (!todoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Todo not found with id: " + id);
        }

        todoRepository.deleteById(id);
        log.info("Todo deleted successfully with id: {}", id);
        return true;
    }

    @Transactional
    @CachePut(value = "todos", key = "#id")
    public Todo completeTodo(Long id) {
        log.info("Completing todo with id: {}", id);

        Todo todo = getTodoById(id);
        todo.complete();

        Todo completedTodo = todoRepository.save(todo);
        log.info("Todo completed successfully with id: {}", completedTodo.getId());
        return completedTodo;
    }

    // Batch loading method for DataLoader
    public Map<Long, List<Todo>> getTodosByUserIds(List<Long> userIds) {
        log.debug("Batch fetching todos for user ids: {}", userIds);
        List<Todo> todos = todoRepository.findByUserIdIn(userIds);

        return todos.stream()
            .collect(Collectors.groupingBy(todo -> todo.getUser().getId()));
    }

    public Long countTodosByUserIdAndStatus(Long userId, TodoStatus status) {
        log.debug("Counting todos for user id: {} with status: {}", userId, status);
        return todoRepository.countByUserIdAndStatus(userId, status);
    }
}
