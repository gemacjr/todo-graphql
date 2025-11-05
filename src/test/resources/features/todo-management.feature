Feature: Todo Management
  As an API user
  I want to manage todos
  So that I can create, read, update, delete, and track tasks

  Background:
    Given the database is clean
    And a user exists with username "testuser"

  Scenario: Create a new todo successfully
    When I create a todo with the following details:
      | title              | description          | priority | status  |
      | Complete project   | Finish the project   | HIGH     | PENDING |
    Then the todo should be created successfully
    And the todo should have the title "Complete project"
    And the todo should have status "PENDING"
    And the todo should have priority "HIGH"

  Scenario: Create todo for non-existent user should fail
    When I attempt to create a todo for a non-existent user
    Then the creation should fail with message "User not found"

  Scenario: Retrieve todo by ID
    Given a todo exists with title "Test Todo"
    When I retrieve the todo by ID
    Then the todo should be returned successfully
    And the todo should have the title "Test Todo"

  Scenario: Retrieve all todos for a user
    Given the following todos exist for the user:
      | title      | status      | priority |
      | Todo 1     | PENDING     | HIGH     |
      | Todo 2     | IN_PROGRESS | MEDIUM   |
      | Todo 3     | COMPLETED   | LOW      |
    When I retrieve all todos for the user
    Then I should receive 3 todos

  Scenario: Retrieve todos by status
    Given the following todos exist for the user:
      | title      | status      | priority |
      | Todo 1     | PENDING     | HIGH     |
      | Todo 2     | PENDING     | MEDIUM   |
      | Todo 3     | COMPLETED   | LOW      |
    When I retrieve todos with status "PENDING"
    Then I should receive 2 todos
    And all todos should have status "PENDING"

  Scenario: Retrieve todos ordered by priority and due date
    Given the following todos exist for the user:
      | title           | priority | dueDate      |
      | Low Priority    | LOW      | 2025-12-01   |
      | High Priority   | HIGH     | 2025-11-15   |
      | Medium Priority | MEDIUM   | 2025-11-20   |
    When I retrieve ordered todos for the user
    Then I should receive 3 todos
    And the first todo should have priority "HIGH"

  Scenario: Update todo status
    Given a todo exists with title "Update Me" and status "PENDING"
    When I update the todo status to "IN_PROGRESS"
    Then the todo should be updated successfully
    And the todo should have status "IN_PROGRESS"

  Scenario: Complete a todo
    Given a todo exists with title "To Complete" and status "PENDING"
    When I complete the todo
    Then the todo should be updated successfully
    And the todo should have status "COMPLETED"
    And the todo should have a completion timestamp

  Scenario: Update todo to completed should set timestamp
    Given a todo exists with title "To Complete" and status "PENDING"
    When I update the todo status to "COMPLETED"
    Then the todo should be updated successfully
    And the todo should have status "COMPLETED"
    And the todo should have a completion timestamp

  Scenario: Delete todo successfully
    Given a todo exists with title "To Delete"
    When I delete the todo
    Then the todo should be deleted successfully
    And the todo should not exist anymore

  Scenario: Delete non-existent todo should fail
    When I attempt to delete a todo with ID 99999
    Then the deletion should fail with message "Todo not found"

  Scenario: Retrieve overdue todos
    Given the following todos exist for the user:
      | title        | status      | dueDate    |
      | Overdue 1    | PENDING     | 2025-10-01 |
      | Overdue 2    | IN_PROGRESS | 2025-10-15 |
      | Not Overdue  | PENDING     | 2025-12-01 |
      | Completed    | COMPLETED   | 2025-10-01 |
    When I retrieve overdue todos
    Then I should receive 2 todos
    And all todos should be overdue

  Scenario: Retrieve overdue todos for a specific user
    Given the following todos exist for the user:
      | title     | status  | dueDate    |
      | Overdue 1 | PENDING | 2025-10-01 |
      | Current   | PENDING | 2025-12-01 |
    When I retrieve overdue todos for the user
    Then I should receive 1 todo
    And the todo should be overdue

  Scenario: Search todos by title
    Given the following todos exist for the user:
      | title                  | description       |
      | Project Meeting        | Discuss plans     |
      | Complete Documentation | Write docs        |
      | Code Review            | Review PR         |
    When I search todos with "Project"
    Then I should receive 1 todo
    And the results should contain title "Project Meeting"

  Scenario: Search todos by description
    Given the following todos exist for the user:
      | title         | description              |
      | Task 1        | Important meeting notes  |
      | Task 2        | Review code              |
      | Task 3        | Meeting with client      |
    When I search todos with "meeting"
    Then I should receive 2 todos

  Scenario: Get todo statistics
    Given the following todos exist for the user:
      | title  | status      | dueDate    |
      | Todo 1 | PENDING     | 2025-12-01 |
      | Todo 2 | PENDING     | 2025-10-01 |
      | Todo 3 | COMPLETED   | 2025-11-01 |
      | Todo 4 | IN_PROGRESS | 2025-11-15 |
      | Todo 5 | CANCELLED   | 2025-11-20 |
    When I retrieve statistics for the user
    Then the total todos should be 5
    And the completed todos should be 1
    And the pending todos should be 2
    And the in progress todos should be 1
    And the cancelled todos should be 1
    And the overdue todos should be 1

  Scenario: Todo validation - title cannot be blank
    When I attempt to create a todo with blank title
    Then the creation should fail due to validation error

  Scenario: Todo validation - title too long
    When I attempt to create a todo with title exceeding 200 characters
    Then the creation should fail due to validation error

  Scenario: Default values for todo creation
    When I create a minimal todo with only title "Minimal Todo"
    Then the todo should be created successfully
    And the todo should have status "PENDING"
    And the todo should have priority "MEDIUM"

  Scenario: User's todo counts
    Given the following todos exist for the user:
      | title  | status    |
      | Todo 1 | PENDING   |
      | Todo 2 | PENDING   |
      | Todo 3 | COMPLETED |
    When I retrieve the user with todo counts
    Then the user should have 3 total todos
    And the user should have 2 pending todos
    And the user should have 1 completed todo

  Scenario: Check if todo is overdue
    Given a todo exists with title "Overdue Todo" and due date "2025-10-01"
    When I retrieve the todo by ID
    Then the todo should be marked as overdue

  Scenario: Check if todo is not overdue
    Given a todo exists with title "Future Todo" and due date "2025-12-31"
    When I retrieve the todo by ID
    Then the todo should not be marked as overdue
