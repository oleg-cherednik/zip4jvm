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

import lombok.Getter;
import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.IOException;
import java.io.RandomAccessFile;

import static java.util.Objects.requireNonNull;

/**
 * @author Oleg Cherednik
 * @since 22.01.2020
 */
public class LittleEndianDataInputFile implements DataInputFile {

    @Getter
    private final SrcZip srcZip;
    @Getter
    private SrcZip.Disk disk;
    private RandomAccessFile in;

    public LittleEndianDataInputFile(SrcZip srcZip) throws IOException {
        this.srcZip = srcZip;
        openDisk(srcZip.getDiskByNo(0));
    }

    @Override
    public long size() {
        return srcZip.getSize();
    }

    @Override
    public long toLong(byte[] buf, int offs, int len) {
        return getLong(buf, offs, len);
    }

    @Override
    public long skip(long bytes) throws IOException {
        long skipped = 0;

        while (bytes > 0) {
            long actual = in.skipBytes((int)Math.min(Integer.MAX_VALUE, bytes));

            skipped += actual;
            bytes -= actual;

            if (bytes == 0 || !openNextDisk())
                break;
        }

        return skipped;
    }

    @Override
    public void seek(long absoluteOffs) throws IOException {
        openDisk(srcZip.getDiskByAbsoluteOffs(absoluteOffs));
        long relativeOffs = absoluteOffs - disk.getAbsoluteOffs();
        in.seek(relativeOffs);
    }

    @Override
    public void seek(int diskNo, long relativeOffs) throws IOException {
        seek(srcZip.getDiskByNo(diskNo).getAbsoluteOffs() + relativeOffs);
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
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
    }

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
        } catch(IOException e) {
            return IOUtils.EOF;
        }
    }

    private boolean openNextDisk() throws IOException {
        if (disk.isLast())
            return false;

        openDisk(requireNonNull(srcZip.getDiskByNo(disk.getNo() + 1)));
        return true;
    }

    private void openDisk(SrcZip.Disk disk) throws IOException {
        if (this.disk == disk)
            return;

        close();
        in = new RandomAccessFile(disk.getPath().toFile(), "r");
        this.disk = disk;
    }

    @Override
    public void close() throws IOException {
        if (in != null)
            in.close();
    }

    public static long getLong(byte[] buf, int offs, int len) {
        long res = 0;

        for (int i = offs + len - 1; i >= offs; i--)
            res = res << 8 | buf[i] & 0xFF;

        return res;
    }

    @Override
    public String toString() {
        return in == null ? "<empty>" : "offs: " + getAbsoluteOffs() + " (0x" + Long.toHexString(getAbsoluteOffs()) + ')';
    }

}
