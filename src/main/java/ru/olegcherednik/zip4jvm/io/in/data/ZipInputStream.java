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
package ru.olegcherednik.zip4jvm.io.in.data;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.io.Endianness;
import ru.olegcherednik.zip4jvm.io.in.file.DataInputFile;
import ru.olegcherednik.zip4jvm.io.in.file.LittleEndianDataInputFile;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 12.12.2019
 */
@Getter
public class ZipInputStream extends BaseDataInput {

    private final DataInputFile dataInputFile;

    public ZipInputStream(SrcZip srcZip) throws IOException {
        dataInputFile = new LittleEndianDataInputFile(srcZip);
    }

    @Override
    public long getAbsoluteOffs() {
        return dataInputFile.getAbsoluteOffs();
    }

    @Override
    public long convertToAbsoluteOffs(int diskNo, long relativeOffs) {
        return dataInputFile.convertToAbsoluteOffs(diskNo, relativeOffs);
    }

    @Override
    public long getDiskRelativeOffs() {
        return dataInputFile.getDiskRelativeOffs();
    }

    @Override
    public SrcZip getSrcZip() {
        return dataInputFile.getSrcZip();
    }

    @Override
    public SrcZip.Disk getDisk() {
        return dataInputFile.getDisk();
    }

    @Override
    public long size() {
        return dataInputFile.size();
    }

    @Override
    public int read(byte[] buf, int offs, int len) {
        return dataInputFile.read(buf, offs, len);
    }

    @Override
    public void seek(int diskNo, long relativeOffs) {
        dataInputFile.seek(diskNo, relativeOffs);
    }

    @Override
    public void close() throws IOException {
        dataInputFile.close();
    }

    @Override
    public Endianness getEndianness() {
        return dataInputFile.getEndiannes();
    }

    // ---------- RandomAccess ----------

    @Override
    public long skip(long bytes) {
        return dataInputFile.skip(bytes);
    }

    @Override
    public void seek(long absoluteOffs) {
        dataInputFile.seek(absoluteOffs);
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return dataInputFile.toString();
    }

}
