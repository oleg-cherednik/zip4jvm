/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.engine.unzip;

import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;
import ru.olegcherednik.zip4jvm.utils.quitely.functions.RunnableWithException;

import org.apache.commons.collections4.CollectionUtils;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Oleg Cherednik
 * @since 28.12.2024
 */
public class UnzipExtractAsyncEngine extends UnzipExtractEngine {

    protected final int totalThreads;

    public UnzipExtractAsyncEngine(PasswordProvider passwordProvider, ZipModel zipModel, int totalThreads) {
        super(passwordProvider, zipModel);
        this.totalThreads = totalThreads <= 0 ? Runtime.getRuntime().availableProcessors() : totalThreads;
    }

    // ---------- UnzipExtractEngine ----------

    @Override
    protected void extractAllEntries(Path dstDir) {
        List<CompletableFuture<Void>> tasks = new LinkedList<>();
        Iterator<ZipEntry> it = zipModel.absOffsAscIterator();

        ConsecutiveAccessDataInputHolder dataInputHolder =
                new ConsecutiveAccessDataInputHolder(this::createConsecutiveDataInput);
        ExecutorService executor = createExecutor();

        try {
            while (it.hasNext()) {
                ZipEntry zipEntry = it.next();
                Path file = dstDir.resolve(zipEntry.getFileName());

                CompletableFuture<Void> task = createCompletableFuture(
                        () -> extractEntry(file, zipEntry, dataInputHolder.get()), executor);

                tasks.add(task);
            }

            tasks.forEach(CompletableFuture::join);
        } finally {
            executor.shutdown();
            dataInputHolder.release();
        }
    }

    @Override
    protected void extractEntryByPrefix(Path dstDir, Set<String> prefixes) {
        assert CollectionUtils.isNotEmpty(prefixes);

        List<CompletableFuture<Void>> tasks = new LinkedList<>();
        Iterator<ZipEntry> it = zipModel.absOffsAscIterator();

        ConsecutiveAccessDataInputHolder dataInputHolder =
                new ConsecutiveAccessDataInputHolder(this::createConsecutiveDataInput);
        ExecutorService executor = createExecutor();

        try {
            while (it.hasNext()) {
                ZipEntry zipEntry = it.next();
                String fileName = getFileName(zipEntry, prefixes);

                if (fileName != null) {
                    Path file = dstDir.resolve(fileName);
                    CompletableFuture<Void> task = createCompletableFuture(
                            () -> extractEntry(file, zipEntry, dataInputHolder.get()), executor);

                    tasks.add(task);
                }
            }

            tasks.forEach(CompletableFuture::join);
        } finally {
            dataInputHolder.release();
            executor.shutdown();
        }
    }

    // ----------

    protected ExecutorService createExecutor() {
        AtomicInteger counter = new AtomicInteger();
        String format = String.format("zip4jvm-extract-%%0%dd", String.valueOf(totalThreads).length());

        ForkJoinPool.ForkJoinWorkerThreadFactory factory = pool -> {
            ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            thread.setName(String.format(format, counter.incrementAndGet()));
            return thread;
        };

        return new ForkJoinPool(totalThreads, factory, null, false);
    }

    protected CompletableFuture<Void> createCompletableFuture(RunnableWithException task, Executor executor) {
        return CompletableFuture.runAsync(() -> Quietly.doRuntime(task), executor);
    }

}
