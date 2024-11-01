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

import ru.olegcherednik.zip4jvm.io.out.file.OffsetOutputStream;
import ru.olegcherednik.zip4jvm.io.writers.ZipModelWriter;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import lombok.AccessLevel;
import lombok.Getter;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 08.03.2019
 */
public class SolidZipDataOutput extends BaseDataOutput {

    protected final ZipModel zipModel;
    @Getter(AccessLevel.PROTECTED)
    protected final OffsetOutputStream out;

    public SolidZipDataOutput(ZipModel zipModel) throws IOException {
        this.zipModel = zipModel;
        out = OffsetOutputStream.create(zipModel.getSrcZip().getPath());
    }

    @Override
    protected void writeInternal(byte[] buf, int offs, int len) throws IOException {
        out.write(buf, offs, len);
    }

    @Override
    public long getRelativeOffs() {
        return out.getRelativeOffs();
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    // ---------- Closeable ----------

    @Override
    public void close() throws IOException {
        new ZipModelWriter(zipModel).write(this);
        out.close();
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return out.toString();
    }

}
