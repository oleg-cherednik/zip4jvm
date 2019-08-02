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
import lombok.NonNull;
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
public class SplitOutputStream extends OutputStream implements DataOutputStream {

    @NonNull
    private final ZipModel zipModel;
    private final long splitLength;

    @NonNull
    private Path zipFile;
    @NonNull
    private LittleEndianWriteFile out;
    private long bytesWrittenForThisPart;
    private int currSplitFileCounter = -1;

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

    public SplitOutputStream(@NonNull Path zipFile, @NonNull ZipModel zipModel) throws FileNotFoundException {
        this(zipFile, zipModel, ZipModel.NO_SPLIT);
    }

    private SplitOutputStream(@NonNull Path zipFile, @NonNull ZipModel zipModel, long splitLength) throws FileNotFoundException {
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

        out.writeBytes((byte)val);
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
        zipModel.getEndCentralDirectory().setOffs(out.getOffs());
        new ZipModelWriter(zipModel).finalizeZipFile(this, true);
        out.close();
    }

    @Override
    public String toString() {
        return "offs: " + out.getOffs();
    }

    @Override
    public long getFilePointer() throws IOException {
        return out.getFilePointer();
    }

    @Override
    public int getCurrSplitFileCounter() {
        return currSplitFileCounter;
    }

    private final Map<String, Long> mark = new HashMap<>();

    @Override
    public void writeWord(int val) throws IOException {
        out.writeWord(val);
    }

    @Override
    public void writeDword(int val) throws IOException {
        out.writeDword(val);
    }

    @Override
    public void writeDword(long val) throws IOException {
        out.writeDword(val);
    }

    @Override
    public void writeQword(long val) throws IOException {
        out.writeQword(val);
    }

    @Override
    public void writeBytes(byte... buf) throws IOException {
        out.writeBytes(buf);
    }

    @Override
    public void writeBytes(byte[] buf, int offs, int len) throws IOException {
        write(buf, offs, len);
    }

    @Override
    public void mark(String id) {
        mark.put(id, out.getOffs());
    }

    @Override
    public long getWrittenBytesAmount(String id) {
        return out.getOffs() - mark.getOrDefault(id, 0L);
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
