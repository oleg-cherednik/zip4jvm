/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireZeroOrPositive;

/**
 * Random access to a multiple regular files.
 *
 * @author Oleg Cherednik
 * @since 22.01.2020
 */
public class SplitRandomAccessDataInput extends BaseRandomAccessDataInput {

    private SrcZip.Disk disk;
    private RandomAccessFile raf;

    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public SplitRandomAccessDataInput(SrcZip srcZip) {
        super(srcZip);
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
        if (this.disk == disk)
            return;

        close();
        raf = Quietly.doRuntime(() -> new RandomAccessFile(disk.getPath().toFile(), "r"));
        this.disk = disk;
    }

    protected long getDiskOffs() {
        try {
            return raf.getFilePointer();
        } catch (IOException e) {
            return IOUtils.EOF;
        }
    }

    // ---------- DataInput ----------

    @Override
    public ByteOrder getByteOrder() {
        return srcZip.getByteOrder();
    }

    @Override
    public long getAbsOffs() {
        return srcZip.getAbsOffs(disk.getNo(), getDiskOffs());
    }

    @Override
    public long skip(long bytes) {
        requireZeroOrPositive(bytes, "skip.bytes");

        long skipped = 0;

        while (bytes > 0) {
            final long skipBytes = bytes;
            long actual = Quietly.doRuntime(() -> raf.skipBytes((int) Math.min(Integer.MAX_VALUE, skipBytes)));

            skipped += actual;
            bytes -= actual;

            if (bytes == 0 || !openNextDisk())
                break;
        }

        return skipped;
    }

    // ---------- RandomAccessDataInput ----------

    @Override
    public void seek(long absOffs) {
        openDisk(srcZip.getDiskByAbsOffs(absOffs));
        long relativeOffs = absOffs - disk.getAbsOffs();
        Quietly.doRuntime(() -> raf.seek(relativeOffs));
    }

    // ---------- ReadBuffer ----------

    @Override
    public int read(byte[] buf, int offs, int len) {
        int res = 0;
        int size = len;

        while (res < len) {
            final int readOffs = offs;
            final int readSize = size;
            int readNow = Quietly.doRuntime(() -> raf.read(buf, readOffs, readSize));

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
    public void close() {
        if (raf != null)
            Quietly.doRuntime(() -> raf.close());
        super.close();
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return PathUtils.getOffsStr(getAbsOffs(), getDiskOffs(), disk.getNo());
    }

}
