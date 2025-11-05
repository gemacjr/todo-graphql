package com.swiftbeard.todo_graphql.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "todos", indexes = {
    @Index(name = "idx_todo_user_id", columnList = "user_id"),
    @Index(name = "idx_todo_status", columnList = "status"),
    @Index(name = "idx_todo_priority", columnList = "priority"),
    @Index(name = "idx_todo_due_date", columnList = "due_date"),
    @Index(name = "idx_todo_user_status", columnList = "user_id,status"),
    @Index(name = "idx_todo_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"user"})
@ToString(exclude = {"user"})
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    @Column(nullable = false, length = 200)
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Column(length = 2000)
    private String description;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TodoStatus status = TodoStatus.PENDING;

    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TodoPriority priority = TodoPriority.MEDIUM;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TodoStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }

    public enum TodoPriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }

    // Business logic method
    public void complete() {
        this.status = TodoStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}
