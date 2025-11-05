package com.swiftbeard.todo_graphql.repository;

import com.swiftbeard.todo_graphql.entity.Todo;
import com.swiftbeard.todo_graphql.entity.Todo.TodoPriority;
import com.swiftbeard.todo_graphql.entity.Todo.TodoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByUserId(Long userId);

    List<Todo> findByUserIdAndStatus(Long userId, TodoStatus status);

    List<Todo> findByUserIdAndPriority(Long userId, TodoPriority priority);

    List<Todo> findByStatus(TodoStatus status);

    @Query("SELECT t FROM Todo t WHERE t.user.id = :userId ORDER BY t.priority DESC, t.dueDate ASC")
    List<Todo> findByUserIdOrderedByPriorityAndDueDate(@Param("userId") Long userId);

    @Query("SELECT t FROM Todo t WHERE t.dueDate < :date AND t.status != 'COMPLETED'")
    List<Todo> findOverdueTodos(@Param("date") LocalDateTime date);

    @Query("SELECT t FROM Todo t WHERE t.user.id = :userId AND t.dueDate < :date AND t.status != 'COMPLETED'")
    List<Todo> findOverdueTodosByUserId(@Param("userId") Long userId, @Param("date") LocalDateTime date);

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user WHERE t.id = :id")
    Optional<Todo> findByIdWithUser(@Param("id") Long id);

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user WHERE t.id IN :ids")
    List<Todo> findByIdInWithUser(@Param("ids") List<Long> ids);

    @Query("SELECT t FROM Todo t WHERE t.user.id = :userId " +
           "AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Todo> searchTodosByUser(@Param("userId") Long userId, @Param("search") String search);

    @Query("SELECT COUNT(t) FROM Todo t WHERE t.user.id = :userId AND t.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") TodoStatus status);

    // Efficient batch loading for DataLoader
    @Query("SELECT t FROM Todo t WHERE t.user.id IN :userIds")
    List<Todo> findByUserIdIn(@Param("userIds") List<Long> userIds);
}
