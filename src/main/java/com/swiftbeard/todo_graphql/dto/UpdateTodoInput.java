package com.swiftbeard.todo_graphql.dto;

import com.swiftbeard.todo_graphql.entity.Todo.TodoPriority;
import com.swiftbeard.todo_graphql.entity.Todo.TodoStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTodoInput {

    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private TodoStatus status;

    private TodoPriority priority;

    private LocalDateTime dueDate;
}
