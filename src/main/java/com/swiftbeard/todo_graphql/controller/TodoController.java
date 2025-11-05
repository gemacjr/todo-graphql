package com.swiftbeard.todo_graphql.controller;

import com.swiftbeard.todo_graphql.dto.CreateTodoInput;
import com.swiftbeard.todo_graphql.dto.UpdateTodoInput;
import com.swiftbeard.todo_graphql.entity.Todo;
import com.swiftbeard.todo_graphql.entity.Todo.TodoStatus;
import com.swiftbeard.todo_graphql.entity.User;
import com.swiftbeard.todo_graphql.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TodoController {

    private final TodoService todoService;

    // Query Mappings
    @QueryMapping
    public Todo todo(@Argument Long id) {
        log.debug("GraphQL query: todo(id: {})", id);
        return todoService.getTodoById(id);
    }

    @QueryMapping
    public List<Todo> todos() {
        log.debug("GraphQL query: todos()");
        return todoService.getAllTodos();
    }

    @QueryMapping
    public List<Todo> todosByUser(@Argument Long userId) {
        log.debug("GraphQL query: todosByUser(userId: {})", userId);
        return todoService.getTodosByUserId(userId);
    }

    @QueryMapping
    public List<Todo> todosByUserAndStatus(@Argument Long userId, @Argument TodoStatus status) {
        log.debug("GraphQL query: todosByUserAndStatus(userId: {}, status: {})", userId, status);
        return todoService.getTodosByUserIdAndStatus(userId, status);
    }

    @QueryMapping
    public List<Todo> todosByUserOrdered(@Argument Long userId) {
        log.debug("GraphQL query: todosByUserOrdered(userId: {})", userId);
        return todoService.getTodosByUserIdOrdered(userId);
    }

    @QueryMapping
    public List<Todo> overdueTodos() {
        log.debug("GraphQL query: overdueTodos()");
        return todoService.getOverdueTodos();
    }

    @QueryMapping
    public List<Todo> overdueTodosByUser(@Argument Long userId) {
        log.debug("GraphQL query: overdueTodosByUser(userId: {})", userId);
        return todoService.getOverdueTodosByUserId(userId);
    }

    @QueryMapping
    public List<Todo> searchTodosByUser(@Argument Long userId, @Argument String search) {
        log.debug("GraphQL query: searchTodosByUser(userId: {}, search: {})", userId, search);
        return todoService.searchTodosByUser(userId, search);
    }

    @QueryMapping
    public TodoStats todoStats(@Argument Long userId) {
        log.debug("GraphQL query: todoStats(userId: {})", userId);
        List<Todo> todos = todoService.getTodosByUserId(userId);

        long completedCount = todos.stream()
            .filter(t -> t.getStatus() == TodoStatus.COMPLETED)
            .count();

        long pendingCount = todos.stream()
            .filter(t -> t.getStatus() == TodoStatus.PENDING)
            .count();

        long inProgressCount = todos.stream()
            .filter(t -> t.getStatus() == TodoStatus.IN_PROGRESS)
            .count();

        long cancelledCount = todos.stream()
            .filter(t -> t.getStatus() == TodoStatus.CANCELLED)
            .count();

        long overdueCount = todos.stream()
            .filter(t -> t.getDueDate() != null &&
                t.getDueDate().isBefore(LocalDateTime.now()) &&
                t.getStatus() != TodoStatus.COMPLETED)
            .count();

        return new TodoStats(
            todos.size(),
            (int) completedCount,
            (int) pendingCount,
            (int) inProgressCount,
            (int) cancelledCount,
            (int) overdueCount
        );
    }

    // Mutation Mappings
    @MutationMapping
    public Todo createTodo(@Argument CreateTodoInput input) {
        log.debug("GraphQL mutation: createTodo(input: {})", input);
        return todoService.createTodo(input);
    }

    @MutationMapping
    public Todo updateTodo(@Argument Long id, @Argument UpdateTodoInput input) {
        log.debug("GraphQL mutation: updateTodo(id: {}, input: {})", id, input);
        return todoService.updateTodo(id, input);
    }

    @MutationMapping
    public Boolean deleteTodo(@Argument Long id) {
        log.debug("GraphQL mutation: deleteTodo(id: {})", id);
        return todoService.deleteTodo(id);
    }

    @MutationMapping
    public Todo completeTodo(@Argument Long id) {
        log.debug("GraphQL mutation: completeTodo(id: {})", id);
        return todoService.completeTodo(id);
    }

    // Field Resolvers
    @SchemaMapping(typeName = "Todo", field = "user")
    public User user(Todo todo) {
        log.debug("GraphQL field resolver: Todo.user for todo id: {}", todo.getId());
        return todo.getUser();
    }

    @SchemaMapping(typeName = "Todo", field = "isOverdue")
    public Boolean isOverdue(Todo todo) {
        log.debug("GraphQL field resolver: Todo.isOverdue for todo id: {}", todo.getId());
        return todo.getDueDate() != null &&
            todo.getDueDate().isBefore(LocalDateTime.now()) &&
            todo.getStatus() != TodoStatus.COMPLETED;
    }

    // Stats DTO
    public record TodoStats(
        int totalTodos,
        int completedTodos,
        int pendingTodos,
        int inProgressTodos,
        int cancelledTodos,
        int overdueTodos
    ) {}
}
