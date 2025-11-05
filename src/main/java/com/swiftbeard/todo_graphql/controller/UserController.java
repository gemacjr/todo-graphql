package com.swiftbeard.todo_graphql.controller;

import com.swiftbeard.todo_graphql.dto.CreateUserInput;
import com.swiftbeard.todo_graphql.dto.UpdateUserInput;
import com.swiftbeard.todo_graphql.entity.Todo;
import com.swiftbeard.todo_graphql.entity.Todo.TodoStatus;
import com.swiftbeard.todo_graphql.entity.User;
import com.swiftbeard.todo_graphql.service.TodoService;
import com.swiftbeard.todo_graphql.service.UserService;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.DataLoader;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final TodoService todoService;

    // Query Mappings
    @QueryMapping
    public User user(@Argument Long id) {
        log.debug("GraphQL query: user(id: {})", id);
        return userService.getUserById(id);
    }

    @QueryMapping
    public User userByEmail(@Argument String email) {
        log.debug("GraphQL query: userByEmail(email: {})", email);
        return userService.getUserByEmail(email);
    }

    @QueryMapping
    public User userByUsername(@Argument String username) {
        log.debug("GraphQL query: userByUsername(username: {})", username);
        return userService.getUserByUsername(username);
    }

    @QueryMapping
    public List<User> users() {
        log.debug("GraphQL query: users()");
        return userService.getAllUsers();
    }

    @QueryMapping
    public List<User> activeUsers() {
        log.debug("GraphQL query: activeUsers()");
        return userService.getActiveUsers();
    }

    @QueryMapping
    public List<User> searchUsers(@Argument String search) {
        log.debug("GraphQL query: searchUsers(search: {})", search);
        return userService.searchUsers(search);
    }

    // Mutation Mappings
    @MutationMapping
    public User createUser(@Argument CreateUserInput input) {
        log.debug("GraphQL mutation: createUser(input: {})", input);
        return userService.createUser(input);
    }

    @MutationMapping
    public User updateUser(@Argument Long id, @Argument UpdateUserInput input) {
        log.debug("GraphQL mutation: updateUser(id: {}, input: {})", id, input);
        return userService.updateUser(id, input);
    }

    @MutationMapping
    public Boolean deleteUser(@Argument Long id) {
        log.debug("GraphQL mutation: deleteUser(id: {})", id);
        return userService.deleteUser(id);
    }

    // Field Resolvers using DataLoader for efficient batching
    @SchemaMapping(typeName = "User", field = "todos")
    public CompletableFuture<List<Todo>> todos(User user, DataFetchingEnvironment environment) {
        log.debug("GraphQL field resolver: User.todos for user id: {}", user.getId());
        DataLoader<Long, List<Todo>> dataLoader = environment.getDataLoader("todosByUser");
        return dataLoader.load(user.getId());
    }

    @SchemaMapping(typeName = "User", field = "todoCount")
    public Integer todoCount(User user) {
        log.debug("GraphQL field resolver: User.todoCount for user id: {}", user.getId());
        return todoService.getTodosByUserId(user.getId()).size();
    }

    @SchemaMapping(typeName = "User", field = "completedTodoCount")
    public Integer completedTodoCount(User user) {
        log.debug("GraphQL field resolver: User.completedTodoCount for user id: {}", user.getId());
        return todoService.countTodosByUserIdAndStatus(user.getId(), TodoStatus.COMPLETED).intValue();
    }

    @SchemaMapping(typeName = "User", field = "pendingTodoCount")
    public Integer pendingTodoCount(User user) {
        log.debug("GraphQL field resolver: User.pendingTodoCount for user id: {}", user.getId());
        return todoService.countTodosByUserIdAndStatus(user.getId(), TodoStatus.PENDING).intValue();
    }
}
