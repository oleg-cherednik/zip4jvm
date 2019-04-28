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
import net.lingala.zip4j.model.AESExtraDataRecord;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.EndCentralDirectory;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.Zip64;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.utils.InternalZipConstants;
import net.lingala.zip4j.utils.Raw;
import org.apache.commons.lang.ArrayUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class SplitOutputStream extends OutputStream {

    private final long splitLength;

    protected Path zipFile;
    protected RandomAccessFile out;
    protected long bytesWrittenForThisPart;
    private int currSplitFileCounter = -1;

    protected SplitOutputStream(Path zipFile, long splitLength) throws FileNotFoundException {
        if (splitLength >= 0 && splitLength < InternalZipConstants.MIN_SPLIT_LENGTH)
            throw new ZipException("split length less than minimum allowed split length of " + InternalZipConstants.MIN_SPLIT_LENGTH + " Bytes");

        this.splitLength = splitLength;
        this.zipFile = zipFile;
        openRandomAccessFile();
    }

    @Override
    public void write(int val) throws IOException {
        write(new byte[] { (byte)val }, 0, 1);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        final int offsInit = offs;

        while (len > 0) {
            int canWrite = (int)(splitLength - bytesWrittenForThisPart);
            int writeCurrPart = Math.min(len, canWrite);

            if (canWrite <= 0 || len > canWrite && offsInit != offs && isSignatureData.test(buf))
                startNextSplitFile();

            _write(buf, offs, writeCurrPart);
            offs += writeCurrPart;
            len -= writeCurrPart;
        }
    }

    @SuppressWarnings("NewMethodNamingConvention")
    protected final void _write(byte[] buf, int offs, int len) throws IOException {
        if (len > 0) {
            out.write(buf, offs, len);
            bytesWrittenForThisPart += len;
        }
    }

    private void startNextSplitFile() throws IOException {
        String zipFileName = zipFile.toAbsolutePath().toString();
        Path currSplitFile = ZipModel.getSplitFilePath(zipFile, currSplitFileCounter + 1);

        out.close();

        if (Files.exists(currSplitFile))
            throw new IOException("split file: " + currSplitFile.getFileName() + " already exists in the current directory, cannot rename this file");

        if (!zipFile.toFile().renameTo(currSplitFile.toFile()))
            throw new IOException("cannot rename newly created split file");

        zipFile = new File(zipFileName).toPath();
        openRandomAccessFile();
    }

    private void openRandomAccessFile() throws FileNotFoundException {
        out = new RandomAccessFile(zipFile.toFile(), "rw");
        currSplitFileCounter++;
        bytesWrittenForThisPart = 0;
    }

    public void seek(long pos) throws IOException {
        out.seek(pos);
    }

    @Override
    public void close() throws IOException {
        if (out != null)
            out.close();
    }

    public long getFilePointer() throws IOException {
        return out.getFilePointer();
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


    public void writeSignature(int val) throws IOException {
        Raw.writeIntLittleEndian(intByte, 0, val);
        write(intByte);
        offs += intByte.length;
    }


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

    @SuppressWarnings("FieldNamingConvention")
    public static final Predicate<byte[]> isSignatureData = new Predicate<byte[]>() {
        private final Set<Integer> signature = new HashSet<>(Arrays.asList(
                LocalFileHeader.SIGNATURE,
                InternalZipConstants.EXTSIG,
                CentralDirectory.FileHeader.SIGNATURE,
                EndCentralDirectory.SIGNATURE,
                CentralDirectory.DigitalSignature.SIGNATURE,
                InternalZipConstants.ARCEXTDATREC,
                InternalZipConstants.SPLITSIG,
                Zip64.EndCentralDirectoryLocator.SIGNATURE,
                Zip64.EndCentralDirectory.SIGNATURE,
                (int)Zip64.ExtendedInfo.SIGNATURE,
                (int)AESExtraDataRecord.SIGNATURE));

        @Override
        public boolean test(byte[] buf) {
            return ArrayUtils.getLength(buf) >= 4 && signature.contains(Raw.readIntLittleEndian(buf, 0));
        }
    };

}
