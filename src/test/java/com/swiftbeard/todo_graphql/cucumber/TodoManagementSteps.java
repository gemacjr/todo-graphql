package com.swiftbeard.todo_graphql.cucumber;

import com.swiftbeard.todo_graphql.controller.TodoController.TodoStats;
import com.swiftbeard.todo_graphql.dto.CreateTodoInput;
import com.swiftbeard.todo_graphql.dto.CreateUserInput;
import com.swiftbeard.todo_graphql.dto.UpdateTodoInput;
import com.swiftbeard.todo_graphql.entity.Todo;
import com.swiftbeard.todo_graphql.entity.Todo.TodoPriority;
import com.swiftbeard.todo_graphql.entity.Todo.TodoStatus;
import com.swiftbeard.todo_graphql.entity.User;
import com.swiftbeard.todo_graphql.service.TodoService;
import com.swiftbeard.todo_graphql.service.UserService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TodoManagementSteps {

    @Autowired
    private TodoService todoService;

    @Autowired
    private UserService userService;

    private User testUser;
    private Todo createdTodo;
    private Todo retrievedTodo;
    private List<Todo> retrievedTodos;
    private TodoStats todoStats;
    private Exception thrownException;
    private Long todoIdToDelete;

    @Given("a user exists with username {string}")
    public void aUserExistsWithUsername(String username) {
        CreateUserInput input = CreateUserInput.builder()
            .username(username)
            .email(username + "@example.com")
            .firstName("Test")
            .lastName("User")
            .build();
        testUser = userService.createUser(input);
    }

    @When("I create a todo with the following details:")
    public void iCreateATodoWithTheFollowingDetails(DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);
        CreateTodoInput input = CreateTodoInput.builder()
            .title(data.get("title"))
            .description(data.get("description"))
            .priority(TodoPriority.valueOf(data.get("priority")))
            .status(TodoStatus.valueOf(data.get("status")))
            .userId(testUser.getId())
            .build();

        createdTodo = todoService.createTodo(input);
    }

    @Then("the todo should be created successfully")
    public void theTodoShouldBeCreatedSuccessfully() {
        assertThat(createdTodo).isNotNull();
        assertThat(createdTodo.getId()).isNotNull();
    }

    @And("the todo should have the title {string}")
    public void theTodoShouldHaveTheTitle(String title) {
        Todo todo = createdTodo != null ? createdTodo : retrievedTodo;
        assertThat(todo).isNotNull();
        assertThat(todo.getTitle()).isEqualTo(title);
    }

    @And("the todo should have status {string}")
    public void theTodoShouldHaveStatus(String status) {
        Todo todo = createdTodo != null ? createdTodo : retrievedTodo;
        assertThat(todo.getStatus()).isEqualTo(TodoStatus.valueOf(status));
    }

    @And("the todo should have priority {string}")
    public void theTodoShouldHavePriority(String priority) {
        Todo todo = createdTodo != null ? createdTodo : retrievedTodo;
        assertThat(todo.getPriority()).isEqualTo(TodoPriority.valueOf(priority));
    }

    @When("I attempt to create a todo for a non-existent user")
    public void iAttemptToCreateATodoForANonExistentUser() {
        CreateTodoInput input = CreateTodoInput.builder()
            .title("Test Todo")
            .userId(99999L)
            .build();

        try {
            todoService.createTodo(input);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("the creation should fail with message {string}")
    public void theCreationShouldFailWithMessage(String message) {
        assertThat(thrownException).isNotNull();
        assertThat(thrownException.getMessage()).contains(message);
    }

    @Given("a todo exists with title {string}")
    public void aTodoExistsWithTitle(String title) {
        CreateTodoInput input = CreateTodoInput.builder()
            .title(title)
            .userId(testUser.getId())
            .priority(TodoPriority.MEDIUM)
            .build();
        createdTodo = todoService.createTodo(input);
    }

    @When("I retrieve the todo by ID")
    public void iRetrieveTheTodoById() {
        retrievedTodo = todoService.getTodoById(createdTodo.getId());
    }

    @Then("the todo should be returned successfully")
    public void theTodoShouldBeReturnedSuccessfully() {
        assertThat(retrievedTodo).isNotNull();
    }

    @Given("the following todos exist for the user:")
    public void theFollowingTodosExistForTheUser(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        for (Map<String, String> row : rows) {
            CreateTodoInput.CreateTodoInputBuilder builder = CreateTodoInput.builder()
                .title(row.get("title"))
                .userId(testUser.getId());

            if (row.containsKey("description")) {
                builder.description(row.get("description"));
            }
            if (row.containsKey("status")) {
                builder.status(TodoStatus.valueOf(row.get("status")));
            }
            if (row.containsKey("priority")) {
                builder.priority(TodoPriority.valueOf(row.get("priority")));
            }
            if (row.containsKey("dueDate")) {
                builder.dueDate(LocalDateTime.parse(row.get("dueDate") + "T00:00:00"));
            }

            todoService.createTodo(builder.build());
        }
    }

    @When("I retrieve all todos for the user")
    public void iRetrieveAllTodosForTheUser() {
        retrievedTodos = todoService.getTodosByUserId(testUser.getId());
    }

    @Then("I should receive {int} todos")
    public void iShouldReceiveTodos(int count) {
        assertThat(retrievedTodos).hasSize(count);
    }

    @When("I retrieve todos with status {string}")
    public void iRetrieveTodosWithStatus(String status) {
        retrievedTodos = todoService.getTodosByUserIdAndStatus(testUser.getId(), TodoStatus.valueOf(status));
    }

    @And("all todos should have status {string}")
    public void allTodosShouldHaveStatus(String status) {
        TodoStatus expectedStatus = TodoStatus.valueOf(status);
        assertThat(retrievedTodos).allMatch(todo -> todo.getStatus() == expectedStatus);
    }

    @When("I retrieve ordered todos for the user")
    public void iRetrieveOrderedTodosForTheUser() {
        retrievedTodos = todoService.getTodosByUserIdOrdered(testUser.getId());
    }

    @And("the first todo should have priority {string}")
    public void theFirstTodoShouldHavePriority(String priority) {
        assertThat(retrievedTodos).isNotEmpty();
        assertThat(retrievedTodos.get(0).getPriority()).isEqualTo(TodoPriority.valueOf(priority));
    }

    @Given("a todo exists with title {string} and status {string}")
    public void aTodoExistsWithTitleAndStatus(String title, String status) {
        CreateTodoInput input = CreateTodoInput.builder()
            .title(title)
            .status(TodoStatus.valueOf(status))
            .userId(testUser.getId())
            .priority(TodoPriority.MEDIUM)
            .build();
        createdTodo = todoService.createTodo(input);
    }

    @When("I update the todo status to {string}")
    public void iUpdateTheTodoStatusTo(String status) {
        UpdateTodoInput input = UpdateTodoInput.builder()
            .status(TodoStatus.valueOf(status))
            .build();
        retrievedTodo = todoService.updateTodo(createdTodo.getId(), input);
    }

    @Then("the todo should be updated successfully")
    public void theTodoShouldBeUpdatedSuccessfully() {
        assertThat(retrievedTodo).isNotNull();
    }

    @When("I complete the todo")
    public void iCompleteTheTodo() {
        retrievedTodo = todoService.completeTodo(createdTodo.getId());
    }

    @And("the todo should have a completion timestamp")
    public void theTodoShouldHaveACompletionTimestamp() {
        assertThat(retrievedTodo.getCompletedAt()).isNotNull();
    }

    @When("I delete the todo")
    public void iDeleteTheTodo() {
        todoIdToDelete = createdTodo.getId();
        todoService.deleteTodo(todoIdToDelete);
    }

    @Then("the todo should be deleted successfully")
    public void theTodoShouldBeDeletedSuccessfully() {
        assertThat(todoIdToDelete).isNotNull();
    }

    @And("the todo should not exist anymore")
    public void theTodoShouldNotExistAnymore() {
        assertThatThrownBy(() -> todoService.getTodoById(todoIdToDelete))
            .hasMessageContaining("Todo not found");
    }

    @When("I attempt to delete a todo with ID {long}")
    public void iAttemptToDeleteATodoWithID(long id) {
        try {
            todoService.deleteTodo(id);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("the deletion should fail with message {string}")
    public void theDeletionShouldFailWithMessage(String message) {
        assertThat(thrownException).isNotNull();
        assertThat(thrownException.getMessage()).contains(message);
    }

    @When("I retrieve overdue todos")
    public void iRetrieveOverdueTodos() {
        retrievedTodos = todoService.getOverdueTodos();
    }

    @And("all todos should be overdue")
    public void allTodosShouldBeOverdue() {
        LocalDateTime now = LocalDateTime.now();
        assertThat(retrievedTodos).allMatch(todo ->
            todo.getDueDate() != null &&
            todo.getDueDate().isBefore(now) &&
            todo.getStatus() != TodoStatus.COMPLETED
        );
    }

    @When("I retrieve overdue todos for the user")
    public void iRetrieveOverdueTodosForTheUser() {
        retrievedTodos = todoService.getOverdueTodosByUserId(testUser.getId());
    }

    @And("the todo should be overdue")
    public void theTodoShouldBeOverdue() {
        Todo todo = retrievedTodos != null && !retrievedTodos.isEmpty() ? retrievedTodos.get(0) : retrievedTodo;
        LocalDateTime now = LocalDateTime.now();
        assertThat(todo.getDueDate()).isBefore(now);
        assertThat(todo.getStatus()).isNotEqualTo(TodoStatus.COMPLETED);
    }

    @And("the todo should not be marked as overdue")
    public void theTodoShouldNotBeMarkedAsOverdue() {
        LocalDateTime now = LocalDateTime.now();
        boolean isOverdue = retrievedTodo.getDueDate() != null &&
            retrievedTodo.getDueDate().isBefore(now) &&
            retrievedTodo.getStatus() != TodoStatus.COMPLETED;
        assertThat(isOverdue).isFalse();
    }

    @When("I search todos with {string}")
    public void iSearchTodosWith(String searchTerm) {
        retrievedTodos = todoService.searchTodosByUser(testUser.getId(), searchTerm);
    }

    @And("the results should contain title {string}")
    public void theResultsShouldContainTitle(String title) {
        assertThat(retrievedTodos)
            .extracting(Todo::getTitle)
            .contains(title);
    }

    @When("I retrieve statistics for the user")
    public void iRetrieveStatisticsForTheUser() {
        List<Todo> todos = todoService.getTodosByUserId(testUser.getId());

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

        todoStats = new TodoStats(
            todos.size(),
            (int) completedCount,
            (int) pendingCount,
            (int) inProgressCount,
            (int) cancelledCount,
            (int) overdueCount
        );
    }

    @Then("the total todos should be {int}")
    public void theTotalTodosShouldBe(int count) {
        assertThat(todoStats.totalTodos()).isEqualTo(count);
    }

    @And("the completed todos should be {int}")
    public void theCompletedTodosShouldBe(int count) {
        assertThat(todoStats.completedTodos()).isEqualTo(count);
    }

    @And("the pending todos should be {int}")
    public void thePendingTodosShouldBe(int count) {
        assertThat(todoStats.pendingTodos()).isEqualTo(count);
    }

    @And("the in progress todos should be {int}")
    public void theInProgressTodosShouldBe(int count) {
        assertThat(todoStats.inProgressTodos()).isEqualTo(count);
    }

    @And("the cancelled todos should be {int}")
    public void theCancelledTodosShouldBe(int count) {
        assertThat(todoStats.cancelledTodos()).isEqualTo(count);
    }

    @And("the overdue todos should be {int}")
    public void theOverdueTodosShouldBe(int count) {
        assertThat(todoStats.overdueTodos()).isEqualTo(count);
    }

    @When("I attempt to create a todo with blank title")
    public void iAttemptToCreateATodoWithBlankTitle() {
        CreateTodoInput input = CreateTodoInput.builder()
            .title("")
            .userId(testUser.getId())
            .priority(TodoPriority.MEDIUM)
            .build();

        try {
            todoService.createTodo(input);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("the creation should fail due to validation error")
    public void theCreationShouldFailDueToValidationError() {
        assertThat(thrownException).isNotNull();
    }

    @When("I attempt to create a todo with title exceeding {int} characters")
    public void iAttemptToCreateATodoWithTitleExceedingCharacters(int length) {
        String longTitle = "a".repeat(length + 1);
        CreateTodoInput input = CreateTodoInput.builder()
            .title(longTitle)
            .userId(testUser.getId())
            .priority(TodoPriority.MEDIUM)
            .build();

        try {
            todoService.createTodo(input);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @When("I create a minimal todo with only title {string}")
    public void iCreateAMinimalTodoWithOnlyTitle(String title) {
        CreateTodoInput input = CreateTodoInput.builder()
            .title(title)
            .userId(testUser.getId())
            .build();
        createdTodo = todoService.createTodo(input);
    }

    @When("I retrieve the user with todo counts")
    public void iRetrieveTheUserWithTodoCounts() {
        testUser = userService.getUserById(testUser.getId());
    }

    @Then("the user should have {int} total todos")
    public void theUserShouldHaveTotalTodos(int count) {
        int actualCount = todoService.getTodosByUserId(testUser.getId()).size();
        assertThat(actualCount).isEqualTo(count);
    }

    @And("the user should have {int} pending todos")
    public void theUserShouldHavePendingTodos(int count) {
        long actualCount = todoService.countTodosByUserIdAndStatus(testUser.getId(), TodoStatus.PENDING);
        assertThat(actualCount).isEqualTo(count);
    }

    @And("the user should have {int} completed todo")
    public void theUserShouldHaveCompletedTodo(int count) {
        long actualCount = todoService.countTodosByUserIdAndStatus(testUser.getId(), TodoStatus.COMPLETED);
        assertThat(actualCount).isEqualTo(count);
    }

    @Given("a todo exists with title {string} and due date {string}")
    public void aTodoExistsWithTitleAndDueDate(String title, String dueDate) {
        CreateTodoInput input = CreateTodoInput.builder()
            .title(title)
            .userId(testUser.getId())
            .priority(TodoPriority.MEDIUM)
            .dueDate(LocalDateTime.parse(dueDate + "T00:00:00"))
            .build();
        createdTodo = todoService.createTodo(input);
    }

    @Then("the todo should be marked as overdue")
    public void theTodoShouldBeMarkedAsOverdue() {
        LocalDateTime now = LocalDateTime.now();
        boolean isOverdue = retrievedTodo.getDueDate() != null &&
            retrievedTodo.getDueDate().isBefore(now) &&
            retrievedTodo.getStatus() != TodoStatus.COMPLETED;
        assertThat(isOverdue).isTrue();
    }

    @Then("the todo should not be marked as overdue")
    public void theTodoShouldNotBeMarkedAsOverdueAgain() {
        LocalDateTime now = LocalDateTime.now();
        boolean isOverdue = retrievedTodo.getDueDate() != null &&
            retrievedTodo.getDueDate().isBefore(now) &&
            retrievedTodo.getStatus() != TodoStatus.COMPLETED;
        assertThat(isOverdue).isFalse();
    }

    @And("the todo should be overdue")
    public void theTodoShouldBeOverdueAgain() {
        theTodoShouldBeOverdue();
    }

    @And("the todo should not be overdue")
    public void theTodoShouldNotBeOverdue() {
        theTodoShouldNotBeMarkedAsOverdue();
    }

    @Then("I should receive {int} todo")
    public void iShouldReceiveTodo(int count) {
        iShouldReceiveTodos(count);
    }
}
