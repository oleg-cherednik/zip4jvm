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
package ru.olegcherednik.zip4jvm.io.in.file.random;

import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;

import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Oleg Cherednik
 * @since 10.11.2024
 */
public class SolidRandomAccessDataInput extends RandomAccessFileBaseDataInput {

    private final SrcZip srcZip;
    private final RandomAccessFile in;

    public SolidRandomAccessDataInput(SrcZip srcZip) throws FileNotFoundException {
        super(srcZip);
        this.srcZip = srcZip;
        in = new RandomAccessFile(srcZip.getDiskByNo(0).getPath().toFile(), "r");
    }

    // ---------- DataInput ----------

    @Override
    public long getAbsOffs() {
        return getDiskOffs();
    }

    @Override
    public long skip(long bytes) throws IOException {
        ValidationUtils.requireZeroOrPositive(bytes, "skip.bytes");
        return in.skipBytes((int) Math.min(Integer.MAX_VALUE, bytes));
    }

    // ---------- RandomAccessFileBaseDataInput ----------

    @Override
    public long getDiskOffs() {
        try {
            return in.getFilePointer();
        } catch (IOException e) {
            return IOUtils.EOF;
        }
    }

    // ---------- RandomAccessDataInput ----------

    @Override
    public void seek(int diskNo, long diskOffs) throws IOException {
        seek(srcZip.getDiskByNo(diskNo).getAbsOffs() + diskOffs);
    }

    // ---------- ReadBuffer ----------

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        return in.read(buf, offs, len);
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        in.close();
    }

    // ---------- RandomAccessDataInput ----------

    @Override
    public void seek(long absOffs) throws IOException {
        in.seek(absOffs);
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return in == null ? "<empty>" : PathUtils.getOffsStr(getAbsOffs());
    }

}
