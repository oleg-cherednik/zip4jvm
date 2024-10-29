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
package ru.olegcherednik.zip4jvm.io.out.data;

import ru.olegcherednik.zip4jvm.io.out.file.DataOutputFile;
import ru.olegcherednik.zip4jvm.io.out.file.LittleEndianWriteFile;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 11.02.2020
 */
abstract class BaseZipDataOutput extends BaseDataOutput {

    protected final ZipModel zipModel;
    private DataOutputFile delegate;

    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    protected BaseZipDataOutput(ZipModel zipModel) throws IOException {
        this.zipModel = zipModel;
        createFile(zipModel.getSrcZip().getPath());
    }

    protected void createFile(Path zip) throws IOException {
        delegate = new LittleEndianWriteFile(zip);
    }

    @Override
    public void fromLong(long val, byte[] buf, int offs, int len) {
        delegate.fromLong(val, buf, offs, len);
    }

    @Override
    public final long getRelativeOffs() {
        return delegate.getRelativeOffs();
    }

    @Override
    protected void writeInternal(byte[] buf, int offs, int len) throws IOException {
        delegate.write(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
