/*
 * Tencent is pleased to support the open source community by making Polaris available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.tencent.polaris.api.rpc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 异步获取实例的应答
 */
public class InstancesFuture implements Future<InstancesResponse>, CompletionStage<InstancesResponse> {

    private final CompletableFuture<InstancesResponse> completableFuture;

    public InstancesFuture(
            CompletableFuture<InstancesResponse> completableFuture) {
        this.completableFuture = completableFuture;
    }

    @Override
    public <U> CompletionStage<U> thenApply(Function<? super InstancesResponse, ? extends U> fn) {
        return completableFuture.thenApply(fn);
    }

    @Override
    public <U> CompletionStage<U> thenApplyAsync(Function<? super InstancesResponse, ? extends U> fn) {
        return completableFuture.thenApplyAsync(fn);
    }

    @Override
    public <U> CompletionStage<U> thenApplyAsync(Function<? super InstancesResponse, ? extends U> fn,
            Executor executor) {
        return completableFuture.thenApplyAsync(fn, executor);
    }

    @Override
    public CompletionStage<Void> thenAccept(Consumer<? super InstancesResponse> action) {
        return completableFuture.thenAccept(action);
    }

    @Override
    public CompletionStage<Void> thenAcceptAsync(Consumer<? super InstancesResponse> action) {
        return completableFuture.thenAcceptAsync(action);
    }

    @Override
    public CompletionStage<Void> thenAcceptAsync(Consumer<? super InstancesResponse> action, Executor executor) {
        return completableFuture.thenAcceptAsync(action, executor);
    }

    @Override
    public CompletionStage<Void> thenRun(Runnable action) {
        return completableFuture.thenRun(action);
    }

    @Override
    public CompletionStage<Void> thenRunAsync(Runnable action) {
        return completableFuture.thenRunAsync(action);
    }

    @Override
    public CompletionStage<Void> thenRunAsync(Runnable action, Executor executor) {
        return completableFuture.thenRunAsync(action, executor);
    }

    @Override
    public <U, V> CompletionStage<V> thenCombine(CompletionStage<? extends U> other,
            BiFunction<? super InstancesResponse, ? super U, ? extends V> fn) {
        return completableFuture.thenCombine(other, fn);
    }

    @Override
    public <U, V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other,
            BiFunction<? super InstancesResponse, ? super U, ? extends V> fn) {
        return completableFuture.thenCombineAsync(other, fn);
    }

    @Override
    public <U, V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other,
            BiFunction<? super InstancesResponse, ? super U, ? extends V> fn, Executor executor) {
        return completableFuture.thenCombineAsync(other, fn, executor);
    }

    @Override
    public <U> CompletionStage<Void> thenAcceptBoth(CompletionStage<? extends U> other,
            BiConsumer<? super InstancesResponse, ? super U> action) {
        return completableFuture.thenAcceptBoth(other, action);
    }

    @Override
    public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,
            BiConsumer<? super InstancesResponse, ? super U> action) {
        return completableFuture.thenAcceptBothAsync(other, action);
    }

    @Override
    public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,
            BiConsumer<? super InstancesResponse, ? super U> action, Executor executor) {
        return completableFuture.thenAcceptBothAsync(other, action, executor);
    }

    @Override
    public CompletionStage<Void> runAfterBoth(CompletionStage<?> other, Runnable action) {
        return completableFuture.runAfterBoth(other, action);
    }

    @Override
    public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) {
        return completableFuture.runAfterBothAsync(other, action);
    }

    @Override
    public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor) {
        return completableFuture.runAfterBothAsync(other, action, executor);
    }

    @Override
    public <U> CompletionStage<U> applyToEither(CompletionStage<? extends InstancesResponse> other,
            Function<? super InstancesResponse, U> fn) {
        return completableFuture.applyToEither(other, fn);
    }

    @Override
    public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends InstancesResponse> other,
            Function<? super InstancesResponse, U> fn) {
        return completableFuture.applyToEitherAsync(other, fn);
    }

    @Override
    public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends InstancesResponse> other,
            Function<? super InstancesResponse, U> fn, Executor executor) {
        return completableFuture.applyToEitherAsync(other, fn, executor);
    }

    @Override
    public CompletionStage<Void> acceptEither(CompletionStage<? extends InstancesResponse> other,
            Consumer<? super InstancesResponse> action) {
        return completableFuture.acceptEither(other, action);
    }

    @Override
    public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends InstancesResponse> other,
            Consumer<? super InstancesResponse> action) {
        return completableFuture.acceptEitherAsync(other, action);
    }

    @Override
    public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends InstancesResponse> other,
            Consumer<? super InstancesResponse> action, Executor executor) {
        return completableFuture.acceptEitherAsync(other, action, executor);
    }

    @Override
    public CompletionStage<Void> runAfterEither(CompletionStage<?> other, Runnable action) {
        return completableFuture.runAfterEither(other, action);
    }

    @Override
    public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action) {
        return completableFuture.runAfterEitherAsync(other, action);
    }

    @Override
    public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor) {
        return completableFuture.runAfterEitherAsync(other, action);
    }

    @Override
    public <U> CompletionStage<U> thenCompose(Function<? super InstancesResponse, ? extends CompletionStage<U>> fn) {
        return completableFuture.thenCompose(fn);
    }

    @Override
    public <U> CompletionStage<U> thenComposeAsync(
            Function<? super InstancesResponse, ? extends CompletionStage<U>> fn) {
        return completableFuture.thenComposeAsync(fn);
    }

    @Override
    public <U> CompletionStage<U> thenComposeAsync(Function<? super InstancesResponse, ? extends CompletionStage<U>> fn,
            Executor executor) {
        return completableFuture.thenComposeAsync(fn, executor);
    }

    @Override
    public CompletionStage<InstancesResponse> exceptionally(Function<Throwable, ? extends InstancesResponse> fn) {
        return completableFuture.exceptionally(fn);
    }

    @Override
    public CompletionStage<InstancesResponse> whenComplete(
            BiConsumer<? super InstancesResponse, ? super Throwable> action) {
        return completableFuture.whenComplete(action);
    }

    @Override
    public CompletionStage<InstancesResponse> whenCompleteAsync(
            BiConsumer<? super InstancesResponse, ? super Throwable> action) {
        return completableFuture.whenCompleteAsync(action);
    }

    @Override
    public CompletionStage<InstancesResponse> whenCompleteAsync(
            BiConsumer<? super InstancesResponse, ? super Throwable> action, Executor executor) {
        return completableFuture.whenCompleteAsync(action, executor);
    }

    @Override
    public <U> CompletionStage<U> handle(BiFunction<? super InstancesResponse, Throwable, ? extends U> fn) {
        return completableFuture.handle(fn);
    }

    @Override
    public <U> CompletionStage<U> handleAsync(BiFunction<? super InstancesResponse, Throwable, ? extends U> fn) {
        return completableFuture.handleAsync(fn);
    }

    @Override
    public <U> CompletionStage<U> handleAsync(BiFunction<? super InstancesResponse, Throwable, ? extends U> fn,
            Executor executor) {
        return completableFuture.handleAsync(fn, executor);
    }

    @Override
    public CompletableFuture<InstancesResponse> toCompletableFuture() {
        return completableFuture.toCompletableFuture();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return completableFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return completableFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return completableFuture.isDone();
    }

    @Override
    public InstancesResponse get() throws InterruptedException, ExecutionException {
        return completableFuture.get();
    }

    @Override
    public InstancesResponse get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return completableFuture.get(timeout, unit);
    }
}
