package io.siggi.cubecore;

import java.util.concurrent.*;
import java.util.function.BiFunction;

public final class AsyncValue<T> {
    private final CompletableFuture<T> future;
    private final Executor executor;
    static Executor defaultExecutor;

    private AsyncValue(CompletableFuture<T> future, Executor executor) {
        if (future == null) {
            throw new NullPointerException("future is null");
        }
        this.future = future;
        this.executor = executor;
    }

    /**
     * Wrap a CompletableFuture. Do not use this to wrap a CompletableFuture that completes on the main thread.
     *
     * @param future the CompletableFuture to wrap
     * @return an AsyncValue wrapping the passed CompletableFuture.
     * @param <T> the value type
     */
    public static <T> AsyncValue<T> of(CompletableFuture<T> future) {
        return new AsyncValue<>(future, defaultExecutor);
    }

    public static AsyncValue<Void> allOf(AsyncValue<?> ...values) {
        CompletableFuture<?>[] futures = new CompletableFuture[values.length];
        for (int i = 0; i < values.length; i++) {
            futures[i] = values[i].getCompletableFuture();
        }
        return of(CompletableFuture.allOf(futures));
    }

    public CompletableFuture<T> getCompletableFuture() {
        return future;
    }

    /**
     * Block the current thread until the value is available.
     *
     * @return the value returned by the get method on the wrapped CompletableFuture.
     * @throws CancellationException if the computation was cancelled
     * @throws CompletionException if this future completed exceptionally or a completion computation threw an exception
     */
    public T get() {
        return future.join();
    }

    /**
     * When the wrapped CompletableFuture is completed, run a function. If the wrapped CompletableFuture is already
     * completed, it will run immediately on the current thread. If the wrapped CompletableFuture is not completed yet,
     * the function will be run later. If the current platform has a main thread (Bukkit) it will run there. If there
     * is no main thread (BungeeCord) it will run on the thread that completes the wrapped CompletableFuture.
     *
     * @param function the intermediate function to call
     * @return the output of the intermediate function wrapped in an AsyncValue
     * @param <U> the output type
     */
    public <U> AsyncValue<U> then(BiFunction<? super T, Throwable, U> function) {
        if (future.isDone())
            return new AsyncValue<>(future.handle(function), executor);
        return new AsyncValue<>(
                executor == null ? future.handle(function) : future.handleAsync(function, executor),
                executor
        );
    }

    /**
     * When the value is resolved, run a function using the passed executor.
     *
     * @param function the intermediate function to call
     * @param executor the executor to run the function on
     * @return the output of the intermediate function wrapped in an AsyncValue
     * @param <U> the output type
     */
    public <U> AsyncValue<U> then(BiFunction<? super T, Throwable, U> function, Executor executor) {
        return new AsyncValue<>(
                future.handleAsync(function, executor),
                this.executor
        );
    }
}
