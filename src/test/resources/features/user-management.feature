Feature: User Management
  As an API user
  I want to manage users
  So that I can create, read, update, and delete user accounts

  Background:
    Given the database is clean

  Scenario: Create a new user successfully
    When I create a user with the following details:
      | username  | email              | firstName | lastName |
      | john_doe  | john@example.com   | John      | Doe      |
    Then the user should be created successfully
    And the user should have the username "john_doe"
    And the user should be active

  Scenario: Create user with duplicate email should fail
    Given a user exists with email "existing@example.com"
    When I attempt to create a user with email "existing@example.com"
    Then the creation should fail with message "User already exists with email"

  Scenario: Create user with duplicate username should fail
    Given a user exists with username "existing_user"
    When I attempt to create a user with username "existing_user"
    Then the creation should fail with message "User already exists with username"

  Scenario: Retrieve user by ID
    Given a user exists with username "test_user"
    When I retrieve the user by ID
    Then the user should be returned successfully
    And the user should have the username "test_user"

  Scenario: Retrieve user by email
    Given a user exists with email "test@example.com"
    When I retrieve the user by email "test@example.com"
    Then the user should be returned successfully
    And the user email should be "test@example.com"

  Scenario: Retrieve user by username
    Given a user exists with username "test_user"
    When I retrieve the user by username "test_user"
    Then the user should be returned successfully
    And the user should have the username "test_user"

  Scenario: Update user information
    Given a user exists with username "john_doe"
    When I update the user's first name to "Jane"
    Then the user should be updated successfully
    And the user's first name should be "Jane"

  Scenario: Update user with duplicate email should fail
    Given a user exists with email "user1@example.com"
    And a user exists with email "user2@example.com"
    When I attempt to update the first user's email to "user2@example.com"
    Then the update should fail with message "User already exists with email"

  Scenario: Delete user successfully
    Given a user exists with username "to_delete"
    When I delete the user
    Then the user should be deleted successfully
    And the user should not exist anymore

  Scenario: Delete non-existent user should fail
    When I attempt to delete a user with ID 99999
    Then the deletion should fail with message "User not found"

  Scenario: List all users
    Given the following users exist:
      | username | email              | firstName | lastName |
      | user1    | user1@example.com  | User      | One      |
      | user2    | user2@example.com  | User      | Two      |
      | user3    | user3@example.com  | User      | Three    |
    When I retrieve all users
    Then I should receive 3 users

  Scenario: List only active users
    Given the following users exist:
      | username | email              | firstName | lastName | isActive |
      | active1  | active1@test.com   | Active    | One      | true     |
      | active2  | active2@test.com   | Active    | Two      | true     |
    And an inactive user exists with username "inactive1"
    When I retrieve active users
    Then I should receive 2 users
    And all users should be active

  Scenario: Search users by username
    Given the following users exist:
      | username    | email                | firstName | lastName |
      | john_doe    | john@example.com     | John      | Doe      |
      | jane_smith  | jane@example.com     | Jane      | Smith    |
      | bob_johnson | bob@example.com      | Bob       | Johnson  |
    When I search for users with "john"
    Then I should receive 2 users
    And the results should contain username "john_doe"
    And the results should contain username "bob_johnson"

  Scenario: Search users by email
    Given the following users exist:
      | username | email              | firstName | lastName |
      | user1    | test1@gmail.com    | User      | One      |
      | user2    | test2@yahoo.com    | User      | Two      |
      | user3    | test3@gmail.com    | User      | Three    |
    When I search for users with "gmail"
    Then I should receive 2 users

  Scenario: User validation - username too short
    When I attempt to create a user with username "ab"
    Then the creation should fail due to validation error

  Scenario: User validation - invalid email format
    When I attempt to create a user with email "invalid-email"
    Then the creation should fail due to validation error
