package com.swiftbeard.todo_graphql.config;

import com.swiftbeard.todo_graphql.entity.Todo;
import com.swiftbeard.todo_graphql.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.DataLoaderRegistrar;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Configuration
@RequiredArgsConstructor
public class DataLoaderConfiguration {

    private final TodoService todoService;

    /**
     * DataLoader for batching todo queries by user ID to prevent N+1 query problem
     */
    @Bean
    public DataLoaderRegistrar dataLoaderRegistrar() {
        return registry -> {
            // TodosByUser DataLoader
            DataLoader<Long, List<Todo>> todosByUserLoader = DataLoader.newMappedDataLoader(
                (userIds) -> CompletableFuture.supplyAsync(() -> {
                    Map<Long, List<Todo>> todosByUserId = todoService.getTodosByUserIds(userIds);
                    return todosByUserId;
                })
            );
            registry.register("todosByUser", todosByUserLoader);
        };
    }
}
