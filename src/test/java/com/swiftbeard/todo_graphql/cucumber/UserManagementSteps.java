package com.swiftbeard.todo_graphql.cucumber;

import com.swiftbeard.todo_graphql.dto.CreateUserInput;
import com.swiftbeard.todo_graphql.dto.UpdateUserInput;
import com.swiftbeard.todo_graphql.entity.User;
import com.swiftbeard.todo_graphql.repository.TodoRepository;
import com.swiftbeard.todo_graphql.repository.UserRepository;
import com.swiftbeard.todo_graphql.service.UserService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserManagementSteps {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TodoRepository todoRepository;

    private User createdUser;
    private User retrievedUser;
    private List<User> retrievedUsers;
    private Exception thrownException;
    private Long userIdToDelete;

    @Given("the database is clean")
    public void theDatabaseIsClean() {
        todoRepository.deleteAll();
        userRepository.deleteAll();
        createdUser = null;
        retrievedUser = null;
        retrievedUsers = null;
        thrownException = null;
        userIdToDelete = null;
    }

    @When("I create a user with the following details:")
    public void iCreateAUserWithTheFollowingDetails(DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);
        CreateUserInput input = CreateUserInput.builder()
            .username(data.get("username"))
            .email(data.get("email"))
            .firstName(data.get("firstName"))
            .lastName(data.get("lastName"))
            .build();

        createdUser = userService.createUser(input);
    }

    @Then("the user should be created successfully")
    public void theUserShouldBeCreatedSuccessfully() {
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();
    }

    @And("the user should have the username {string}")
    public void theUserShouldHaveTheUsername(String username) {
        User user = createdUser != null ? createdUser : retrievedUser;
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo(username);
    }

    @And("the user should be active")
    public void theUserShouldBeActive() {
        assertThat(createdUser.getIsActive()).isTrue();
    }

    @Given("a user exists with email {string}")
    public void aUserExistsWithEmail(String email) {
        CreateUserInput input = CreateUserInput.builder()
            .username("existing_" + System.currentTimeMillis())
            .email(email)
            .firstName("Existing")
            .lastName("User")
            .build();
        createdUser = userService.createUser(input);
    }

    @When("I attempt to create a user with email {string}")
    public void iAttemptToCreateAUserWithEmail(String email) {
        CreateUserInput input = CreateUserInput.builder()
            .username("newuser")
            .email(email)
            .firstName("New")
            .lastName("User")
            .build();

        try {
            userService.createUser(input);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("the creation should fail with message {string}")
    public void theCreationShouldFailWithMessage(String message) {
        assertThat(thrownException).isNotNull();
        assertThat(thrownException.getMessage()).contains(message);
    }

    @Given("a user exists with username {string}")
    public void aUserExistsWithUsername(String username) {
        CreateUserInput input = CreateUserInput.builder()
            .username(username)
            .email(username + "@example.com")
            .firstName("Test")
            .lastName("User")
            .build();
        createdUser = userService.createUser(input);
    }

    @When("I attempt to create a user with username {string}")
    public void iAttemptToCreateAUserWithUsername(String username) {
        CreateUserInput input = CreateUserInput.builder()
            .username(username)
            .email("new@example.com")
            .firstName("New")
            .lastName("User")
            .build();

        try {
            userService.createUser(input);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @When("I retrieve the user by ID")
    public void iRetrieveTheUserById() {
        retrievedUser = userService.getUserById(createdUser.getId());
    }

    @Then("the user should be returned successfully")
    public void theUserShouldBeReturnedSuccessfully() {
        assertThat(retrievedUser).isNotNull();
    }

    @When("I retrieve the user by email {string}")
    public void iRetrieveTheUserByEmail(String email) {
        retrievedUser = userService.getUserByEmail(email);
    }

    @And("the user email should be {string}")
    public void theUserEmailShouldBe(String email) {
        assertThat(retrievedUser.getEmail()).isEqualTo(email);
    }

    @When("I retrieve the user by username {string}")
    public void iRetrieveTheUserByUsername(String username) {
        retrievedUser = userService.getUserByUsername(username);
    }

    @When("I update the user's first name to {string}")
    public void iUpdateTheUserSFirstNameTo(String firstName) {
        UpdateUserInput input = UpdateUserInput.builder()
            .firstName(firstName)
            .build();
        retrievedUser = userService.updateUser(createdUser.getId(), input);
    }

    @Then("the user should be updated successfully")
    public void theUserShouldBeUpdatedSuccessfully() {
        assertThat(retrievedUser).isNotNull();
    }

    @And("the user's first name should be {string}")
    public void theUserSFirstNameShouldBe(String firstName) {
        assertThat(retrievedUser.getFirstName()).isEqualTo(firstName);
    }

    @When("I attempt to update the first user's email to {string}")
    public void iAttemptToUpdateTheFirstUserSEmailTo(String email) {
        UpdateUserInput input = UpdateUserInput.builder()
            .email(email)
            .build();

        try {
            userService.updateUser(createdUser.getId(), input);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("the update should fail with message {string}")
    public void theUpdateShouldFailWithMessage(String message) {
        assertThat(thrownException).isNotNull();
        assertThat(thrownException.getMessage()).contains(message);
    }

    @When("I delete the user")
    public void iDeleteTheUser() {
        userIdToDelete = createdUser.getId();
        userService.deleteUser(userIdToDelete);
    }

    @Then("the user should be deleted successfully")
    public void theUserShouldBeDeletedSuccessfully() {
        // No exception thrown means success
        assertThat(userIdToDelete).isNotNull();
    }

    @And("the user should not exist anymore")
    public void theUserShouldNotExistAnymore() {
        assertThatThrownBy(() -> userService.getUserById(userIdToDelete))
            .hasMessageContaining("User not found");
    }

    @When("I attempt to delete a user with ID {long}")
    public void iAttemptToDeleteAUserWithID(long id) {
        try {
            userService.deleteUser(id);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("the deletion should fail with message {string}")
    public void theDeletionShouldFailWithMessage(String message) {
        assertThat(thrownException).isNotNull();
        assertThat(thrownException.getMessage()).contains(message);
    }

    @Given("the following users exist:")
    public void theFollowingUsersExist(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        for (Map<String, String> row : rows) {
            CreateUserInput input = CreateUserInput.builder()
                .username(row.get("username"))
                .email(row.get("email"))
                .firstName(row.get("firstName"))
                .lastName(row.get("lastName"))
                .build();
            User user = userService.createUser(input);

            // Handle isActive if provided
            if (row.containsKey("isActive")) {
                boolean isActive = Boolean.parseBoolean(row.get("isActive"));
                UpdateUserInput updateInput = UpdateUserInput.builder()
                    .isActive(isActive)
                    .build();
                userService.updateUser(user.getId(), updateInput);
            }
        }
    }

    @When("I retrieve all users")
    public void iRetrieveAllUsers() {
        retrievedUsers = userService.getAllUsers();
    }

    @Then("I should receive {int} users")
    public void iShouldReceiveUsers(int count) {
        assertThat(retrievedUsers).hasSize(count);
    }

    @Given("an inactive user exists with username {string}")
    public void anInactiveUserExistsWithUsername(String username) {
        CreateUserInput input = CreateUserInput.builder()
            .username(username)
            .email(username + "@example.com")
            .firstName("Inactive")
            .lastName("User")
            .build();
        User user = userService.createUser(input);

        UpdateUserInput updateInput = UpdateUserInput.builder()
            .isActive(false)
            .build();
        userService.updateUser(user.getId(), updateInput);
    }

    @When("I retrieve active users")
    public void iRetrieveActiveUsers() {
        retrievedUsers = userService.getActiveUsers();
    }

    @And("all users should be active")
    public void allUsersShouldBeActive() {
        assertThat(retrievedUsers).allMatch(User::getIsActive);
    }

    @When("I search for users with {string}")
    public void iSearchForUsersWith(String searchTerm) {
        retrievedUsers = userService.searchUsers(searchTerm);
    }

    @And("the results should contain username {string}")
    public void theResultsShouldContainUsername(String username) {
        assertThat(retrievedUsers)
            .extracting(User::getUsername)
            .contains(username);
    }

    @When("I attempt to create a user with username {string}")
    public void iAttemptToCreateAUserWithUsernameShort(String username) {
        CreateUserInput input = CreateUserInput.builder()
            .username(username)
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .build();

        try {
            userService.createUser(input);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("the creation should fail due to validation error")
    public void theCreationShouldFailDueToValidationError() {
        assertThat(thrownException).isNotNull();
    }

    @When("I attempt to create a user with email {string}")
    public void iAttemptToCreateAUserWithEmailInvalid(String email) {
        CreateUserInput input = CreateUserInput.builder()
            .username("testuser")
            .email(email)
            .firstName("Test")
            .lastName("User")
            .build();

        try {
            userService.createUser(input);
        } catch (Exception e) {
            thrownException = e;
        }
    }
}
