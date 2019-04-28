/*
 * Copyright 2010 Srikanth Reddy Lingala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.lingala.zip4j.io;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.utils.InternalZipConstants;
import net.lingala.zip4j.utils.Raw;
import net.lingala.zip4j.utils.ZipUtils;
import org.apache.commons.lang.ArrayUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SplitOutputStream extends OutputStream {

    protected RandomAccessFile raf;
    private final long splitLength;
    private Path zipFile;
    private int currSplitFileCounter;
    protected long bytesWrittenForThisPart;

    protected SplitOutputStream(Path zipFile, long splitLength) throws FileNotFoundException {
        if (splitLength >= 0 && splitLength < InternalZipConstants.MIN_SPLIT_LENGTH)
            throw new ZipException("split length less than minimum allowed split length of " + InternalZipConstants.MIN_SPLIT_LENGTH + " Bytes");

        this.splitLength = splitLength;
        raf = new RandomAccessFile(zipFile.toFile(), "rw");
        this.zipFile = zipFile;
    }

    @Override
    public void write(int val) throws IOException {
        write(new byte[] { (byte)val }, 0, 1);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        if (len <= 0)
            return;

        if (bytesWrittenForThisPart >= splitLength) {
            startNextSplitFile();
            raf.write(buf, offs, len);
            bytesWrittenForThisPart = len;
        } else if (bytesWrittenForThisPart + len <= splitLength) {
            raf.write(buf, offs, len);
            bytesWrittenForThisPart += len;
        } else if (isHeaderData(buf)) {
            startNextSplitFile();
            raf.write(buf, offs, len);
            bytesWrittenForThisPart = len;
        } else {
            raf.write(buf, offs, (int)(splitLength - bytesWrittenForThisPart));
            startNextSplitFile();
            raf.write(buf, offs + (int)(splitLength - bytesWrittenForThisPart), (int)(len - (splitLength - bytesWrittenForThisPart)));
            bytesWrittenForThisPart = len - (splitLength - bytesWrittenForThisPart);
        }
    }

    private void startNextSplitFile() throws IOException {
        String zipFileName = zipFile.toAbsolutePath().toString();
        Path currSplitFile = ZipModel.getSplitFilePath(zipFile, currSplitFileCounter + 1);

        raf.close();

        if (Files.exists(currSplitFile))
            throw new IOException("split file: " + currSplitFile.getFileName() + " already exists in the current directory, cannot rename this file");

        if (!zipFile.toFile().renameTo(currSplitFile.toFile()))
            throw new IOException("cannot rename newly created split file");

        zipFile = new File(zipFileName).toPath();
        raf = new RandomAccessFile(zipFile.toFile(), "rw");
        currSplitFileCounter++;
    }

    public void seek(long pos) throws IOException {
        raf.seek(pos);
    }

    @Override
    public void close() throws IOException {
        if (raf != null)
            raf.close();
    }

    public long getFilePointer() throws IOException {
        return raf.getFilePointer();
    }

    public int getCurrSplitFileCounter() {
        return currSplitFileCounter;
    }

    private final byte[] intByte = new byte[4];
    private final byte[] shortByte = new byte[2];
    private final byte[] longByte = new byte[8];

    @Getter
    @Setter
    private long offs;
    private final Map<String, Long> mark = new HashMap<>();

    public void writeWord(short val) throws IOException {
        Raw.writeShortLittleEndian(shortByte, 0, val);
        write(shortByte);
        offs += shortByte.length;
    }

    public void writeDword(int val) throws IOException {
        Raw.writeIntLittleEndian(intByte, 0, val);
        write(intByte);
        offs += intByte.length;
    }

    public void writeDword(long val) throws IOException {
        Raw.writeLongLittleEndian(longByte, 0, val);
        System.arraycopy(longByte, 0, intByte, 0, 4);
        write(intByte);
        offs += intByte.length;
    }

    public void writeQword(long val) throws IOException {
        Raw.writeLongLittleEndian(longByte, 0, val);
        write(longByte);
        offs += longByte.length;
    }

    public void writeBytes(byte... buf) throws IOException {
        if (buf == null)
            return;
        write(buf);
        offs += buf.length;
    }

    public void writeBytes(byte[] buf, int offs, int len) throws IOException {
        if (buf == null)
            return;
        write(buf, offs, len);
        this.offs += len;
    }

    public void mark(String id) {
        mark.put(id, offs);
    }

    public long getWrittenBytesAmount(String id) {
        return offs - mark.getOrDefault(id, 0L);
    }

    @NonNull
    public static SplitOutputStream create(@NonNull ZipModel zipModel) throws IOException {
        Path zipFile = zipModel.getZipFile();
        Path parent = zipFile.getParent();

        if (parent != null)
            Files.createDirectories(parent);

        return zipModel.isSplitArchive() ? new SplitOutputStream(zipFile, zipModel.getSplitLength()) : new NoSplitOutputStream(zipFile);
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    private static boolean isHeaderData(byte[] buf) {
        if (ArrayUtils.getLength(buf) < 4)
            return false;

        int signature = Raw.readIntLittleEndian(buf, 0);

        for (long headerSignature : ZipUtils.getAllHeaderSignatures())
            if (headerSignature != InternalZipConstants.SPLITSIG && headerSignature == signature)
                return true;

        return false;
    }

}
