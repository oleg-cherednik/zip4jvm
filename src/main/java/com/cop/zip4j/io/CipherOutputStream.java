package com.cop.zip4j.io;

import com.cop.zip4j.core.writers.ZipModelWriter;
import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.crypto.aes.AesEngine;
import com.cop.zip4j.io.delegate.OutputDelegate;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.ZipParameters;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
@RequiredArgsConstructor
public class CipherOutputStream extends OutputStream {

    public static final String MARK = "entry";

    @NonNull
    public final SplitOutputStream out;
    @NonNull
    public final ZipModel zipModel;

    public CentralDirectory.FileHeader fileHeader;
    public LocalFileHeader localFileHeader;
    @NonNull
    public Encoder encoder = Encoder.NULL;
    @NonNull
    public Encryption encryption = Encryption.OFF;

    public final CRC32 crc = new CRC32();
    public final byte[] pendingBuffer = new byte[AesEngine.AES_BLOCK_SIZE];
    public int pendingBufferLength;
    public long totalBytesRead;

    @Setter
    protected OutputDelegate delegate;

    public void putNextEntry(@NonNull PathZipEntry entry, @NonNull ZipParameters parameters) {
        delegate.putNextEntry(entry, parameters);
    }

    @Override
    public void write(int bval) throws IOException {
        byte[] b = new byte[1];
        b[0] = (byte)bval;
        write(b, 0, 1);
    }

    @Override
    public void write(byte[] buf) throws IOException {
        if (ArrayUtils.isNotEmpty(buf))
            write(buf, 0, buf.length);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        delegate.write(buf, offs, len);
    }

    public void closeEntry() throws IOException {
        delegate.closeEntry();
    }

    @Override
    public void close() throws IOException {
        zipModel.getEndCentralDirectory().setOffs(out.getOffs());
        new ZipModelWriter(zipModel).finalizeZipFile(out, true);
        out.close();
    }

    public void seek(long pos) throws IOException {
        out.seek(pos);
    }
}
