# TODO GraphQL API

A comprehensive, production-ready GraphQL API for managing users and todos with best practices for low latency and high availability.

## Features

### Core Features
- **User Management**: Full CRUD operations for users
- **Todo Management**: Complete todo management with status tracking
- **GraphQL API**: Modern GraphQL interface with powerful querying capabilities
- **Search**: Full-text search for users and todos
- **Statistics**: Todo statistics per user

### Performance & Scalability
- **DataLoader Integration**: Prevents N+1 query problems with efficient batch loading
- **Caching**: Multi-level caching with Caffeine cache
  - User cache
  - Todo cache
  - User todos cache
- **Database Optimization**:
  - Strategic indexes on frequently queried columns
  - Connection pooling with HikariCP
  - Batch processing for insert/update operations
  - Query optimization with JPA

### High Availability
- **Health Checks**: Spring Boot Actuator endpoints
  - `/actuator/health` - Application health
  - `/actuator/health/liveness` - Liveness probe
  - `/actuator/health/readiness` - Readiness probe
- **Monitoring**: Prometheus metrics exposed at `/actuator/prometheus`
- **Graceful Degradation**: Proper exception handling and error responses
- **HTTP/2 Support**: Enabled for better performance
- **Response Compression**: Automatic compression for GraphQL responses

### Best Practices
- **Input Validation**: Comprehensive validation on all inputs
- **DTO Pattern**: Separation of input/output from domain models
- **Proper Exception Handling**: Custom GraphQL error handling
- **Logging**: Structured logging with SLF4J
- **Transaction Management**: Proper transaction boundaries
- **Code Quality**: Lombok for reduced boilerplate

## Tech Stack

- **Java 17**
- **Spring Boot 3.5.7**
- **Spring GraphQL**
- **Spring Data JPA**
- **PostgreSQL** (production-ready database via Docker)
- **H2 Database** (in-memory for quick testing)
- **Caffeine Cache**
- **GraphQL Java DataLoader**
- **Lombok**
- **Micrometer** (Prometheus metrics)
- **Spring Boot Actuator**
- **Docker & Docker Compose**

## Getting Started

### Prerequisites
- Java 17 or higher
- Gradle
- Docker and Docker Compose (for PostgreSQL)

### Running with Docker (PostgreSQL)

This is the recommended setup for local development and testing with seed data.

```bash
# Start PostgreSQL database using Docker Compose
docker-compose up -d

# Wait for PostgreSQL to be ready (about 10 seconds)
# You can check the status with:
docker-compose ps

# Run the application with dev profile (includes seed data)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

The Docker Compose setup includes:
- **PostgreSQL**: Running on port 5432
- **pgAdmin**: Web interface for database management on `http://localhost:5050`
  - Email: `admin@todo.com`
  - Password: `admin`

### Running with H2 (In-Memory)

For quick testing without Docker:

```bash
# Build the project
./gradlew build

# Run the application (uses H2 in-memory database)
./gradlew bootRun
```

### Seed Data

When running with the `dev` or `test` profile, the application automatically loads seed data:

**Users** (5 users):
- johndoe (john.doe@example.com)
- janesmith (jane.smith@example.com)
- bobwilson (bob.wilson@example.com)
- alicejones (alice.jones@example.com)
- charliebrown (charlie.brown@example.com) - inactive user

**Todos** (17 todos with various statuses, priorities, and due dates)

### Access Points

- **GraphQL Endpoint**: `http://localhost:8080/graphql`
- **GraphiQL UI**: `http://localhost:8080/graphiql`
- **Health Check**: `http://localhost:8080/actuator/health`
- **Prometheus Metrics**: `http://localhost:8080/actuator/prometheus`

**With PostgreSQL:**
- **pgAdmin**: `http://localhost:5050` (admin@todo.com / admin)

**With H2:**
- **H2 Console**: `http://localhost:8080/h2-console`

### Testing with Postman

A comprehensive Postman collection is available in the `postman/` directory:

1. **Import the collection**:
   - Open Postman
   - Click "Import"
   - Select `postman/TODO-GraphQL-API.postman_collection.json`

2. **Import the environment** (optional):
   - Import `postman/TODO-GraphQL-Environment.postman_environment.json`
   - Select "TODO GraphQL Environment" from the environment dropdown

3. **Start testing**:
   - All queries and mutations are organized by category
   - Each request includes example variables
   - Test with seeded data (User IDs 1-5, Todo IDs 1-17)

**Collection includes**:
- User Queries (6 requests)
- Todo Queries (9 requests)
- User Mutations (3 requests)
- Todo Mutations (4 requests)
- Complex Queries (2 requests)

### Stopping the Services

```bash
# Stop and remove Docker containers
docker-compose down

# Stop and remove containers with volumes (clears database data)
docker-compose down -v
```

## GraphQL Schema

### Types

#### User
```graphql
type User {
    id: ID!
    username: String!
    email: String!
    firstName: String
    lastName: String
    isActive: Boolean!
    todos: [Todo!]!
    todoCount: Int!
    completedTodoCount: Int!
    pendingTodoCount: Int!
    createdAt: String!
    updatedAt: String!
}
```

#### Todo
```graphql
type Todo {
    id: ID!
    title: String!
    description: String
    status: TodoStatus!
    priority: TodoPriority!
    dueDate: String
    completedAt: String
    user: User!
    createdAt: String!
    updatedAt: String!
    isOverdue: Boolean!
}
```

### Queries

#### User Queries
```graphql
# Get user by ID
query {
    user(id: 1) {
        id
        username
        email
        todos {
            id
            title
            status
        }
    }
}

# Get all users
query {
    users {
        id
        username
        email
    }
}

# Search users
query {
    searchUsers(search: "john") {
        id
        username
        email
    }
}
```

#### Todo Queries
```graphql
# Get todos by user
query {
    todosByUser(userId: 1) {
        id
        title
        status
        priority
        user {
            username
        }
    }
}

# Get overdue todos
query {
    overdueTodos {
        id
        title
        dueDate
        user {
            username
        }
    }
}

# Get todo statistics
query {
    todoStats(userId: 1) {
        totalTodos
        completedTodos
        pendingTodos
        overdueTodos
    }
}
```

### Mutations

#### User Mutations
```graphql
# Create user
mutation {
    createUser(input: {
        username: "johndoe"
        email: "john@example.com"
        firstName: "John"
        lastName: "Doe"
    }) {
        id
        username
        email
    }
}

# Update user
mutation {
    updateUser(id: 1, input: {
        firstName: "Jonathan"
        isActive: true
    }) {
        id
        username
        firstName
    }
}

# Delete user
mutation {
    deleteUser(id: 1)
}
```

#### Todo Mutations
```graphql
# Create todo
mutation {
    createTodo(input: {
        title: "Complete project"
        description: "Finish the GraphQL API"
        userId: 1
        priority: HIGH
        status: PENDING
    }) {
        id
        title
        status
        priority
    }
}

# Update todo
mutation {
    updateTodo(id: 1, input: {
        status: IN_PROGRESS
        priority: URGENT
    }) {
        id
        title
        status
        priority
    }
}

# Complete todo
mutation {
    completeTodo(id: 1) {
        id
        title
        status
        completedAt
    }
}

# Delete todo
mutation {
    deleteTodo(id: 1)
}
```

## Database Schema

### Users Table
- `id` (Primary Key)
- `username` (Unique, Indexed)
- `email` (Unique, Indexed)
- `first_name`
- `last_name`
- `is_active`
- `created_at` (Indexed)
- `updated_at`

### Todos Table
- `id` (Primary Key)
- `title`
- `description`
- `status` (Indexed)
- `priority` (Indexed)
- `due_date` (Indexed)
- `completed_at`
- `user_id` (Foreign Key, Indexed)
- `created_at` (Indexed)
- `updated_at`

### Composite Indexes
- `(user_id, status)` - For efficient filtering of user's todos by status

## Performance Optimizations

### Low Latency Strategies
1. **DataLoader**: Batches and caches database queries to prevent N+1 problems
2. **Caching**: Three-tier caching strategy (users, todos, userTodos)
3. **Database Indexes**: Strategic indexes on frequently queried columns
4. **Connection Pooling**: Optimized HikariCP settings
5. **Batch Processing**: Hibernate batch operations enabled
6. **HTTP/2**: Enabled for multiplexing
7. **Compression**: Response compression for GraphQL

### High Availability Features
1. **Health Checks**: Liveness and readiness probes
2. **Monitoring**: Prometheus metrics
3. **Graceful Error Handling**: Proper exception handling with meaningful errors
4. **Transaction Management**: Proper transaction boundaries
5. **Connection Pool Resilience**: Configured with timeouts and proper sizing

## Monitoring

### Health Check Response
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

### Prometheus Metrics
Available at `/actuator/prometheus` including:
- JVM metrics
- HTTP request metrics
- Database connection pool metrics
- Cache metrics
- Custom application metrics

## Error Handling

The API provides clear, structured error messages:

```json
{
  "errors": [
    {
      "message": "User not found with id: 999",
      "locations": [{"line": 2, "column": 3}],
      "path": ["user"],
      "extensions": {
        "classification": "NOT_FOUND"
      }
    }
  ]
}
```

## Testing

Run tests with:
```bash
./gradlew test
```

## Production Considerations

For production deployment:

1. **Database Configuration**:
   - PostgreSQL is already configured (see `application-dev.yaml`)
   - Update database credentials using environment variables:
     ```yaml
     spring.datasource.url=${DB_URL}
     spring.datasource.username=${DB_USERNAME}
     spring.datasource.password=${DB_PASSWORD}
     ```
   - Change `ddl-auto` from `create-drop` to `validate` or `none`
   - Use database migration tools (Flyway or Liquibase)

2. **Security**:
   - Add authentication/authorization (Spring Security + JWT)
   - Enable HTTPS
   - Configure CORS policies
   - Secure database credentials
   - Update pgAdmin access (change default credentials)

3. **Configuration**:
   - Externalize configuration using environment variables
   - Use production profile: `--spring.profiles.active=prod`
   - Secure sensitive data with secret management tools
   - Disable seed data in production (only runs on dev/test profiles)

4. **Monitoring**:
   - Integrate with Prometheus + Grafana
   - Set up alerting for database connections, API latency
   - Configure centralized logging (ELK stack or similar)
   - Monitor database performance via pgAdmin or other tools

5. **Scaling**:
   - Use distributed caching (Redis)
   - Configure multiple instances with load balancing
   - Consider read replicas for PostgreSQL
   - Use connection pooling (already configured with HikariCP)
   - Implement database sharding for large datasets

6. **Docker Deployment**:
   - Use production-grade PostgreSQL configuration
   - Set resource limits in docker-compose
   - Use Docker secrets for sensitive data
   - Configure automated backups for PostgreSQL
   - Consider managed database services (AWS RDS, Azure Database, etc.)

## License

MIT License
