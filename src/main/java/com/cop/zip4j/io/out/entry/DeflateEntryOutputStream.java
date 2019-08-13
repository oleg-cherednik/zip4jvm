package com.cop.zip4j.io.out.entry;

import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.utils.InternalZipConstants;

import java.io.IOException;
import java.util.zip.Deflater;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
final class DeflateEntryOutputStream extends EntryOutputStream {

    private final byte[] buf = new byte[InternalZipConstants.BUF_SIZE];
    private final Deflater deflater = new Deflater();

    public boolean firstBytesRead;

    public DeflateEntryOutputStream(ZipModel zipModel, CentralDirectory.FileHeader fileHeader, Encoder encoder, DataOutput out,
            CompressionLevel compressionLevel) {
        super(zipModel, fileHeader, encoder, out);
        deflater.setLevel(compressionLevel.getValue());
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        updateChecksum(buf, offs, len);
        deflater.setInput(buf, offs, len);

        while (!deflater.needsInput()) {
            deflate();
        }
    }

    private void deflate() throws IOException {
        int len = deflater.deflate(buf, 0, buf.length);

        if (len <= 0)
            return;

        if (deflater.finished()) {
            if (len == 4)
                return;
            if (len < 4)
                return;
            len -= 4;
        }

        if (firstBytesRead)
            encoder._write(buf, 0, len, out);
        else {
            encoder._write(buf, 2, len - 2, out);
            firstBytesRead = true;
        }
    }

    private void finish() throws IOException {
        if (deflater.finished())
            return;

        deflater.finish();

        while (!deflater.finished()) {
            deflate();
        }
    }

    @Override
    public void close() throws IOException {
        finish();
        super.close();
    }
}
