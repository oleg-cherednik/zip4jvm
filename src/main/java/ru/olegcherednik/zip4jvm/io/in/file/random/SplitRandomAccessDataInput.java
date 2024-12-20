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
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.Getter;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireZeroOrPositive;

/**
 * @author Oleg Cherednik
 * @since 22.01.2020
 */
public class SplitRandomAccessDataInput extends RandomAccessFileBaseDataInput {

    protected final SrcZip srcZip;
    private SrcZip.Disk disk;
    private RandomAccessFile in;

    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public SplitRandomAccessDataInput(SrcZip srcZip) {
        super(srcZip);
        this.srcZip = srcZip;
        Quietly.doQuietly(() -> openDisk(srcZip.getDiskByNo(0)));
    }

    private boolean openNextDisk() throws IOException {
        if (disk.isLast())
            return false;

        openDisk(Objects.requireNonNull(srcZip.getDiskByNo(disk.getNo() + 1)));
        return true;
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private void openDisk(SrcZip.Disk disk) throws IOException {
        if (this.disk == disk)
            return;

        close();
        in = new RandomAccessFile(disk.getPath().toFile(), "r");
        this.disk = disk;
    }

    // ---------- DataInput ----------

    @Override
    public long getAbsOffs() {
        return srcZip.getAbsOffs(disk.getNo(), getDiskOffs());
    }

    @Override
    public long skip(long bytes) throws IOException {
        requireZeroOrPositive(bytes, "skip.bytes");

        long skipped = 0;

        while (bytes > 0) {
            long actual = in.skipBytes((int) Math.min(Integer.MAX_VALUE, bytes));

            skipped += actual;
            bytes -= actual;

            if (bytes == 0 || !openNextDisk())
                break;
        }

        return skipped;
    }

    // ---------- RandomAccessDataInput ----------

    @Override
    public void seek(long absOffs) throws IOException {
        openDisk(srcZip.getDiskByAbsOffs(absOffs));
        long relativeOffs = absOffs - disk.getAbsOffs();
        in.seek(relativeOffs);
    }

    // ---------- ReadBuffer ----------

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        int res = 0;
        int size = len;

        while (res < len) {
            int readNow = in.read(buf, offs, size);

            if (readNow > 0)
                res += readNow;

            if (readNow == IOUtils.EOF || readNow < size) {
                if (!openNextDisk())
                    break;

                offs += Math.max(0, readNow);
                size -= Math.max(0, readNow);
            }
        }

        return res;
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        if (in != null)
            in.close();
        super.close();
    }

    // ---------- DataInputFile ----------

    @Override
    public long getDiskOffs() {
        try {
            return in.getFilePointer();
        } catch (IOException e) {
            return IOUtils.EOF;
        }
    }

    @Override
    public void seek(int diskNo, long diskOffs) throws IOException {
        seek(srcZip.getAbsOffs(diskNo, diskOffs));
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return PathUtils.getOffsStr(getAbsOffs());
    }

}
