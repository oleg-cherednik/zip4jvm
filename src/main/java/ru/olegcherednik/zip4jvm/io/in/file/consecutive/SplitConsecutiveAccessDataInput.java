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
package ru.olegcherednik.zip4jvm.io.in.file.consecutive;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import lombok.Getter;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireZeroOrPositive;

/**
 * @author Oleg Cherednik
 * @since 20.12.2024
 */
public class SplitConsecutiveAccessDataInput extends BaseConsecutiveAccessDataInput {

    private final SrcZip srcZip;
    @Getter
    private SrcZip.Disk disk;
    private InputStream in;
    private long diskOffs;

    public SplitConsecutiveAccessDataInput(SrcZip srcZip) throws IOException {
        this.srcZip = srcZip;
        openDisk(srcZip.getDiskByNo(0));
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
        in = new BufferedInputStream(Files.newInputStream(disk.getPath()));
        this.disk = disk;
        diskOffs = 0;
    }

    private void incOffs(long bytes) {
        diskOffs += bytes;
        incAbsOffs(bytes);
    }

    // ---------- DataInput ----------

    @Override
    public ByteOrder getByteOrder() {
        return srcZip.getByteOrder();
    }

    @Override
    public long skip(long bytes) throws IOException {
        requireZeroOrPositive(bytes, "skip.bytes");

        long skipped = 0;

        while (bytes > 0) {
            long skipNow = in.skip(bytes);

            skipped += skipNow;
            bytes -= skipNow;
            incOffs(skipNow);

            if (bytes == 0 || !openNextDisk())
                break;
        }

        return skipped;
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        int res = 0;
        int size = len;

        while (res < len) {
            int readNow = in.read(buf, offs, size);

            if (readNow > 0) {
                res += readNow;
                incOffs(readNow);
            }

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

    // ---------- Object ----------

    @Override
    public String toString() {
        return in == null ? "<empty>" : PathUtils.getOffsStr(getAbsOffs(), diskOffs, disk.getNo());
    }

}
