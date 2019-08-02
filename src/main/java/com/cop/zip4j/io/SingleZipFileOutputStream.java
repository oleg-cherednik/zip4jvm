package com.cop.zip4j.io;

import com.cop.zip4j.core.writers.ZipModelWriter;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.EndCentralDirectory;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.aes.AesExtraDataRecord;
import com.cop.zip4j.utils.InternalZipConstants;
import lombok.NonNull;
import org.apache.commons.lang.ArrayUtils;

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
public class SingleZipFileOutputStream extends OutputStream implements DataOutputStream {

    @NonNull
    private final ZipModel zipModel;
    @NonNull
    private final LittleEndianWriteFile out;

    @NonNull
    public static SingleZipFileOutputStream create(@NonNull ZipModel zipModel) throws IOException {
        Path zipFile = zipModel.getZipFile();
        Path parent = zipFile.getParent();

        if (parent != null)
            Files.createDirectories(parent);

        SingleZipFileOutputStream out = new SingleZipFileOutputStream(zipFile, zipModel);
        out.seek(zipModel.getOffsCentralDirectory());
        return out;
    }

    public SingleZipFileOutputStream(@NonNull Path zipFile, @NonNull ZipModel zipModel) throws FileNotFoundException {
        this.zipModel = zipModel;
        out = new LittleEndianWriteFile(zipFile);
    }

    @Override
    public void write(int val) throws IOException {
        out.writeBytes((byte)val);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        out.write(buf, offs, len);
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
        return 0;
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
