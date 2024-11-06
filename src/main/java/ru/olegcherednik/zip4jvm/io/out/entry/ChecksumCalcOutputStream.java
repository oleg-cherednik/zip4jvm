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
package ru.olegcherednik.zip4jvm.io.out.entry;

import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.PureJavaCrc32;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Checksum;

/**
 * This decorator over the {@link OutputStream} dynamically calculates
 * checksum and uncompressed size of the data.
 *
 * @author Oleg Cherednik
 * @since 28.10.2024
 */
@RequiredArgsConstructor
public class ChecksumCalcOutputStream extends OutputStream {

    private final ZipEntry zipEntry;
    private final OutputStream os;
    private final Checksum checksum = new PureJavaCrc32();

    @Override
    public final void write(int b) throws IOException {
        checksum.update(b);
        os.write(b);
    }

    @Override
    public void close() throws IOException {
        zipEntry.setChecksum(checksum.getValue());
        os.close();
    }

    @Override
    public String toString() {
        return os.toString();
    }

}
