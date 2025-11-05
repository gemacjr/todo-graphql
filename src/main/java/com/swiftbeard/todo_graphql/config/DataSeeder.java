package com.swiftbeard.todo_graphql.config;

import com.swiftbeard.todo_graphql.entity.Todo;
import com.swiftbeard.todo_graphql.entity.User;
import com.swiftbeard.todo_graphql.repository.TodoRepository;
import com.swiftbeard.todo_graphql.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data seeder for development and testing environments.
 * Populates the database with sample users and todos.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    @Bean
    @Profile({"dev", "test"})
    CommandLineRunner initDatabase(UserRepository userRepository, TodoRepository todoRepository) {
        return args -> {
            log.info("Starting database seeding...");

            // Clear existing data
            todoRepository.deleteAll();
            userRepository.deleteAll();

            // Create sample users
            List<User> users = createSampleUsers(userRepository);
            log.info("Created {} sample users", users.size());

            // Create sample todos for each user
            int totalTodos = createSampleTodos(users, todoRepository);
            log.info("Created {} sample todos", totalTodos);

            log.info("Database seeding completed successfully!");
        };
    }

    private List<User> createSampleUsers(UserRepository userRepository) {
        List<User> users = new ArrayList<>();

        users.add(User.builder()
                .username("johndoe")
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .build());

        users.add(User.builder()
                .username("janesmith")
                .email("jane.smith@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .isActive(true)
                .build());

        users.add(User.builder()
                .username("bobwilson")
                .email("bob.wilson@example.com")
                .firstName("Bob")
                .lastName("Wilson")
                .isActive(true)
                .build());

        users.add(User.builder()
                .username("alicejones")
                .email("alice.jones@example.com")
                .firstName("Alice")
                .lastName("Jones")
                .isActive(true)
                .build());

        users.add(User.builder()
                .username("charliebrown")
                .email("charlie.brown@example.com")
                .firstName("Charlie")
                .lastName("Brown")
                .isActive(false)
                .build());

        return userRepository.saveAll(users);
    }

    private int createSampleTodos(List<User> users, TodoRepository todoRepository) {
        List<Todo> allTodos = new ArrayList<>();

        // User 1: John Doe - Mixed priorities and statuses
        User johnDoe = users.get(0);
        allTodos.add(createTodo(
                "Complete project proposal",
                "Draft and submit the Q4 project proposal document",
                Todo.TodoStatus.IN_PROGRESS,
                Todo.TodoPriority.HIGH,
                LocalDateTime.now().plusDays(3),
                johnDoe
        ));

        allTodos.add(createTodo(
                "Review pull requests",
                "Review and merge pending pull requests from team members",
                Todo.TodoStatus.PENDING,
                Todo.TodoPriority.MEDIUM,
                LocalDateTime.now().plusDays(1),
                johnDoe
        ));

        allTodos.add(createTodo(
                "Update documentation",
                "Update API documentation with new endpoints",
                Todo.TodoStatus.COMPLETED,
                Todo.TodoPriority.LOW,
                LocalDateTime.now().minusDays(2),
                johnDoe
        ));

        allTodos.add(createTodo(
                "Fix critical bug",
                "Resolve the authentication timeout issue reported in production",
                Todo.TodoStatus.IN_PROGRESS,
                Todo.TodoPriority.URGENT,
                LocalDateTime.now().plusHours(6),
                johnDoe
        ));

        allTodos.add(createTodo(
                "Team meeting preparation",
                "Prepare slides and agenda for Monday's team meeting",
                Todo.TodoStatus.PENDING,
                Todo.TodoPriority.MEDIUM,
                LocalDateTime.now().plusDays(2),
                johnDoe
        ));

        // User 2: Jane Smith - Mostly completed tasks
        User janeSmith = users.get(1);
        allTodos.add(createTodo(
                "Client presentation",
                "Present new features to the client stakeholders",
                Todo.TodoStatus.COMPLETED,
                Todo.TodoPriority.HIGH,
                LocalDateTime.now().minusDays(1),
                janeSmith
        ));

        allTodos.add(createTodo(
                "Database optimization",
                "Optimize slow queries in the user service",
                Todo.TodoStatus.COMPLETED,
                Todo.TodoPriority.MEDIUM,
                LocalDateTime.now().minusDays(5),
                janeSmith
        ));

        allTodos.add(createTodo(
                "Security audit",
                "Conduct quarterly security audit and update dependencies",
                Todo.TodoStatus.IN_PROGRESS,
                Todo.TodoPriority.HIGH,
                LocalDateTime.now().plusDays(7),
                janeSmith
        ));

        // User 3: Bob Wilson - Mix of overdue and upcoming tasks
        User bobWilson = users.get(2);
        allTodos.add(createTodo(
                "Write unit tests",
                "Increase test coverage for the payment module",
                Todo.TodoStatus.PENDING,
                Todo.TodoPriority.MEDIUM,
                LocalDateTime.now().minusDays(2),
                bobWilson
        ));

        allTodos.add(createTodo(
                "Implement new feature",
                "Add real-time notifications using WebSocket",
                Todo.TodoStatus.IN_PROGRESS,
                Todo.TodoPriority.HIGH,
                LocalDateTime.now().plusDays(10),
                bobWilson
        ));

        allTodos.add(createTodo(
                "Code review workshop",
                "Conduct code review best practices workshop",
                Todo.TodoStatus.CANCELLED,
                Todo.TodoPriority.LOW,
                LocalDateTime.now().plusDays(5),
                bobWilson
        ));

        // User 4: Alice Jones - Urgent and high-priority tasks
        User aliceJones = users.get(3);
        allTodos.add(createTodo(
                "Deploy to production",
                "Deploy version 2.0 to production environment",
                Todo.TodoStatus.PENDING,
                Todo.TodoPriority.URGENT,
                LocalDateTime.now().plusHours(12),
                aliceJones
        ));

        allTodos.add(createTodo(
                "Customer support escalation",
                "Handle escalated customer support ticket #5234",
                Todo.TodoStatus.IN_PROGRESS,
                Todo.TodoPriority.URGENT,
                LocalDateTime.now().plusHours(3),
                aliceJones
        ));

        allTodos.add(createTodo(
                "Performance testing",
                "Run load tests on the new microservices",
                Todo.TodoStatus.PENDING,
                Todo.TodoPriority.HIGH,
                LocalDateTime.now().plusDays(4),
                aliceJones
        ));

        allTodos.add(createTodo(
                "Update CI/CD pipeline",
                "Migrate CI/CD to new infrastructure",
                Todo.TodoStatus.PENDING,
                Todo.TodoPriority.MEDIUM,
                LocalDateTime.now().plusDays(14),
                aliceJones
        ));

        // User 5: Charlie Brown (inactive user) - Some pending tasks
        User charlieBrown = users.get(4);
        allTodos.add(createTodo(
                "Archive old projects",
                "Archive completed projects from last year",
                Todo.TodoStatus.PENDING,
                Todo.TodoPriority.LOW,
                LocalDateTime.now().plusDays(30),
                charlieBrown
        ));

        allTodos.add(createTodo(
                "Backup documentation",
                "Create backup of all project documentation",
                Todo.TodoStatus.COMPLETED,
                Todo.TodoPriority.LOW,
                LocalDateTime.now().minusDays(10),
                charlieBrown
        ));

        // Set completedAt for completed todos
        allTodos.stream()
                .filter(todo -> todo.getStatus() == Todo.TodoStatus.COMPLETED)
                .forEach(todo -> todo.setCompletedAt(LocalDateTime.now().minusDays(1)));

        return todoRepository.saveAll(allTodos).size();
    }

    private Todo createTodo(String title, String description, Todo.TodoStatus status,
                           Todo.TodoPriority priority, LocalDateTime dueDate, User user) {
        return Todo.builder()
                .title(title)
                .description(description)
                .status(status)
                .priority(priority)
                .dueDate(dueDate)
                .user(user)
                .build();
    }
}
