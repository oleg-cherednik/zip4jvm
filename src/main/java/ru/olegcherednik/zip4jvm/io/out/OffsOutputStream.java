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
package ru.olegcherednik.zip4jvm.io.out;

import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This is a decorator for {@link OutputStream} only to control {@link #toString()}
 * result.
 *
 * @author Oleg Cherednik
 * @since 08.08.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class OffsOutputStream extends OutputStream {

    private final OutputStream out;
    @Getter
    private long offs;

    public static OffsOutputStream create(Path file) {
        return Quietly.doRuntime(() -> {
            Files.createDirectories(file.getParent());
            return new OffsOutputStream(new BufferedOutputStream(Files.newOutputStream(file)));
        });
    }

    // ---------- OutputStream ----------

    @Override
    public void write(int b) {
        Quietly.doRuntime(() -> out.write(b));
        offs++;
    }

    @Override
    public void flush() {
        Quietly.doRuntime(out::flush);
    }

    @Override
    public void close() {
        Quietly.doRuntime(out::close);
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return "offs: " + offs + " (0x" + Long.toHexString(offs) + ')';
    }

}
