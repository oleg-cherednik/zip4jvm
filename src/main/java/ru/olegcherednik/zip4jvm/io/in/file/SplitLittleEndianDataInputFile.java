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
package ru.olegcherednik.zip4jvm.io.in.file;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.in.data.BaseDataInput;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.Getter;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 22.01.2020
 */
public class SplitLittleEndianDataInputFile extends BaseDataInput implements DataInputFile {

    @Getter
    private final SrcZip srcZip;

    @Getter
    private SrcZip.Disk disk;
    private RandomAccessFile in;

    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public SplitLittleEndianDataInputFile(SrcZip srcZip) {
        super(ByteOrder.LITTLE_ENDIAN);
        this.srcZip = srcZip;
        openDisk(srcZip.getDiskByNo(0));
    }

    private boolean openNextDisk() {
        if (disk.isLast())
            return false;

        openDisk(Objects.requireNonNull(srcZip.getDiskByNo(disk.getNo() + 1)));
        return true;
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private void openDisk(SrcZip.Disk disk) {
        try {
            if (this.disk == disk)
                return;

            close();
            in = new RandomAccessFile(disk.getPath().toFile(), "r");
            this.disk = disk;
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    // ---------- Closeable ----------

    @Override
    public void close() throws IOException {
        if (in != null)
            in.close();
    }

    // ---------- ReadBuffer ----------

    @Override
    public int read(byte[] buf, int offs, int len) {
        try {
            int res = 0;
            int size = len;

            while (res < len) {
                int totalRead = in.read(buf, offs, size);

                if (totalRead > 0)
                    res += totalRead;

                if (totalRead == IOUtils.EOF || totalRead < size) {
                    if (!openNextDisk())
                        break;

                    offs += Math.max(0, totalRead);
                    size -= Math.max(0, totalRead);
                }
            }

            return res;
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    // ---------- RandomAccess ----------

    @Override
    public long skip(long bytes) {
        ValidationUtils.requireZeroOrPositive(bytes, "skip.bytes");

        try {
            long skipped = 0;

            while (bytes > 0) {
                long actual = in.skipBytes((int) Math.min(Integer.MAX_VALUE, bytes));

                skipped += actual;
                bytes -= actual;

                if (bytes == 0 || !openNextDisk())
                    break;
            }

            return skipped;
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    @Override
    public void seek(long absoluteOffs) {
        Quietly.doQuietly(() -> {
            openDisk(srcZip.getDiskByAbsoluteOffs(absoluteOffs));
            long relativeOffs = absoluteOffs - disk.getAbsoluteOffs();
            in.seek(relativeOffs);
        });
    }

    // ---------- DataInputFile ----------

    @Override
    public long getAbsoluteOffs() {
        return disk.getAbsoluteOffs() + getDiskRelativeOffs();
    }

    @Override
    public long convertToAbsoluteOffs(int diskNo, long relativeOffs) {
        return srcZip.getDiskByNo(diskNo).getAbsoluteOffs() + relativeOffs;
    }

    @Override
    public long getDiskRelativeOffs() {
        try {
            return in.getFilePointer();
        } catch (IOException e) {
            return IOUtils.EOF;
        }
    }

    @Override
    public long size() {
        return srcZip.getSize();
    }

    @Override
    public void seek(int diskNo, long relativeOffs) {
        seek(srcZip.getDiskByNo(diskNo).getAbsoluteOffs() + relativeOffs);
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        if (in == null)
            return "<empty>";

        long offs = getAbsoluteOffs();
        return String.format("offs: %s (0x%s)", offs, Long.toHexString(offs));
    }

}
