package com.cop.zip4j.io;

import com.cop.zip4j.core.writers.ZipModelWriter;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.EndCentralDirectory;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.aes.AesExtraDataRecord;
import com.cop.zip4j.utils.InternalZipConstants;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang.ArrayUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Oleg Cherednik
 * @since 08.03.2019
 */
@SuppressWarnings("SpellCheckingInspection")
public class SplitOutputStream extends OutputStream {

    @NonNull
    public final ZipModel zipModel;
    private final long splitLength;

    @NonNull
    private Path zipFile;
    @NonNull
    private LittleEndianWriteFile out;
    private long bytesWrittenForThisPart;
    private int currSplitFileCounter = -1;

    public SplitOutputStream(@NonNull Path zipFile, @NonNull ZipModel zipModel) throws FileNotFoundException {
        this(zipFile, zipModel, ZipModel.NO_SPLIT);
    }

    public SplitOutputStream(@NonNull Path zipFile, @NonNull ZipModel zipModel, long splitLength) throws FileNotFoundException {
        // TODO move to ZipParameters
        if (splitLength >= 0 && splitLength < InternalZipConstants.MIN_SPLIT_LENGTH)
            throw new Zip4jException("split length less than minimum allowed split length of " + InternalZipConstants.MIN_SPLIT_LENGTH + " Bytes");

        this.zipModel = zipModel;
        this.splitLength = splitLength;
        this.zipFile = zipFile;
        openRandomAccessFile();
    }

    private void openRandomAccessFile() throws FileNotFoundException {
        out = new LittleEndianWriteFile(zipFile);
        currSplitFileCounter++;
        bytesWrittenForThisPart = 0;
    }

    @Override
    public void write(int val) throws IOException {
        if (splitLength != ZipModel.NO_SPLIT && (int)(splitLength - bytesWrittenForThisPart) <= 0)
            startNextSplitFile();

        out.write(val);
        bytesWrittenForThisPart++;
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        final int offsInit = offs;

        while (len > 0) {
            int canWrite = splitLength == ZipModel.NO_SPLIT ? Integer.MAX_VALUE : (int)(splitLength - bytesWrittenForThisPart);
            int writeBytes = Math.min(len, canWrite);

            if (canWrite <= 0 || len > canWrite && offsInit != offs && isSignatureData.test(buf))
                startNextSplitFile();

            out.write(buf, offs, writeBytes);
            bytesWrittenForThisPart += writeBytes;

            offs += writeBytes;
            len -= writeBytes;
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

    public void seek(long pos) throws IOException {
        out.seek(pos);
    }

    @Override
    public void close() throws IOException {
        zipModel.getEndCentralDirectory().setOffs(offs);
        new ZipModelWriter(zipModel).finalizeZipFile(this, true);
        out.close();
    }

    @Override
    public String toString() {
        return "offs: " + offs;
    }

    public long getFilePointer() throws IOException {
        return out.getFilePointer();
    }

    public int getCurrSplitFileCounter() {
        return currSplitFileCounter;
    }

    private final byte[] word = new byte[2];
    private final byte[] dword = new byte[4];
    private final byte[] qword = new byte[8];

    @Getter
    @Setter
    private long offs;
    private final Map<String, Long> mark = new HashMap<>();

    public void writeWord(int val) throws IOException {
        word[0] = (byte)(val & 0xFF);
        word[1] = (byte)(val >>> 8);
        write(word);
        offs += word.length;
    }

    public void writeDword(int val) throws IOException {
        writeDword((long)val);
    }

    public void writeDword(long val) throws IOException {
        dword[0] = (byte)(val & 0xFF);
        dword[1] = (byte)(val >>> 8);
        dword[2] = (byte)(val >>> 16);
        dword[3] = (byte)(val >>> 24);
        write(dword);
        offs += dword.length;
    }

    public void writeQword(long val) throws IOException {
        qword[0] = (byte)(val & 0xFF);
        qword[1] = (byte)(val >>> 8);
        qword[2] = (byte)(val >>> 16);
        qword[3] = (byte)(val >>> 24);
        qword[4] = (byte)(val >>> 32);
        qword[5] = (byte)(val >>> 40);
        qword[6] = (byte)(val >>> 48);
        qword[7] = (byte)(val >>> 56);
        write(qword);
        offs += qword.length;
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

        SplitOutputStream out = new SplitOutputStream(zipFile, zipModel, zipModel.getSplitLength());
        out.seek(zipModel.getOffsCentralDirectory());
        return out;
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
                Zip64.ExtendedInfo.SIGNATURE,
                AesExtraDataRecord.SIGNATURE));

        @Override
        public boolean test(byte[] buf) {
            return ArrayUtils.getLength(buf) >= 4 && signature.contains(buf[3] << 24 | buf[2] << 16 | buf[1] << 8 | buf[0]);
        }
    };

}
