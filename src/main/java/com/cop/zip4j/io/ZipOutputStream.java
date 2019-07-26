package com.cop.zip4j.io;

import com.cop.zip4j.core.writers.ZipModelWriter;
import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.crypto.aes.AesEngine;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;

import java.io.IOException;
import java.util.zip.CRC32;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public class ZipOutputStream implements AutoCloseable {

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

    public ZipOutputStream(@NonNull SplitOutputStream out, @NonNull ZipModel zipModel) throws IOException {
        this.out = out;
        this.zipModel = zipModel;
        out.seek(zipModel.getOffsCentralDirectory());
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
