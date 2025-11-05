package com.swiftbeard.todo_graphql.dto;

import com.swiftbeard.todo_graphql.entity.Todo.TodoPriority;
import com.swiftbeard.todo_graphql.entity.Todo.TodoStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateTodoInput {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "User ID is required")
    private Long userId;

    @Builder.Default
    private TodoStatus status = TodoStatus.PENDING;

    @Builder.Default
    private TodoPriority priority = TodoPriority.MEDIUM;

    private LocalDateTime dueDate;
}
