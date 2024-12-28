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

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.file.consecutive.ConsecutiveAccessDataInput;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * This class is designed to use with custom {@link ExecutorService} only. It holds a list of all {@link DataInput}
 * were create in a different threads. When method {@link #release()} is invoked, it closes all created
 * {@link DataInput}, but it does not clear {@link #THREAD_LOCAL} for all threads. I.e. after invoking
 * {@link #release()} and then invoking {@link #get()}, the given {@link DataInput} will not be {@literal null}, but it
 * will be closed and not available to reuse.
 *
 * @author Oleg Cherednik
 * @since 28.12.2024
 */
@RequiredArgsConstructor
public class ConsecutiveAccessDataInputHolder {

    private static final ThreadLocal<ConsecutiveAccessDataInput> THREAD_LOCAL = new ThreadLocal<>();

    private final List<ConsecutiveAccessDataInput> dataInputs = new CopyOnWriteArrayList<>();

    private final Supplier<ConsecutiveAccessDataInput> dataInputSupplier;

    public void release() {
        // cannot clear all THREAD_LOCAL here
        dataInputs.forEach(IOUtils::closeQuietly);
        dataInputs.clear();
    }

    public ConsecutiveAccessDataInput get() {
        ConsecutiveAccessDataInput in = THREAD_LOCAL.get();

        if (in == null) {
            in = dataInputSupplier.get();
            THREAD_LOCAL.set(in);
            dataInputs.add(in);
        }

        return in;
    }

}
