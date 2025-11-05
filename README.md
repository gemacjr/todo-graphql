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
- **H2 Database** (in-memory for development)
- **Caffeine Cache**
- **GraphQL Java DataLoader**
- **Lombok**
- **Micrometer** (Prometheus metrics)
- **Spring Boot Actuator**

## Getting Started

### Prerequisites
- Java 17 or higher
- Gradle

### Build and Run

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The application will start on `http://localhost:8080`

### Access Points

- **GraphQL Endpoint**: `http://localhost:8080/graphql`
- **GraphiQL UI**: `http://localhost:8080/graphiql`
- **H2 Console**: `http://localhost:8080/h2-console`
- **Health Check**: `http://localhost:8080/actuator/health`
- **Prometheus Metrics**: `http://localhost:8080/actuator/prometheus`

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

1. **Replace H2 with Production Database**:
   - PostgreSQL, MySQL, or other RDBMS
   - Update `application.yaml` with production database settings

2. **Security**:
   - Add authentication/authorization (Spring Security + JWT)
   - Enable HTTPS
   - Configure CORS policies

3. **Configuration**:
   - Externalize configuration
   - Use environment-specific profiles
   - Secure sensitive data

4. **Monitoring**:
   - Integrate with Prometheus + Grafana
   - Set up alerting
   - Configure centralized logging

5. **Scaling**:
   - Use distributed caching (Redis)
   - Configure multiple instances with load balancing
   - Consider read replicas for database

## License

MIT License
