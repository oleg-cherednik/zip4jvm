package com.cop.zip4j.io.entry;

import com.cop.zip4j.io.MarkDataOutput;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.utils.InternalZipConstants;
import lombok.NonNull;

import java.io.IOException;
import java.util.zip.Deflater;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
final class DeflateEntryOutputStream extends EntryOutputStream {

    private final byte[] buf = new byte[InternalZipConstants.BUFF_SIZE];
    private final Deflater deflater = new Deflater();

    public boolean firstBytesRead;

    public DeflateEntryOutputStream(@NonNull ZipModel zipModel, @NonNull MarkDataOutput out, @NonNull CompressionLevel compressionLevel) {
        super(zipModel, out);
        deflater.setLevel(compressionLevel.getValue());
    }

    @Override
    protected void writeImpl(byte[] buf, int offs, int len) throws IOException {
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
            _write(buf, 0, len);
        else {
            _write(buf, 2, len - 2);
            firstBytesRead = true;
        }
    }

    @Override
    public void close() throws IOException {
        if (!deflater.finished()) {
            deflater.finish();

            while (!deflater.finished()) {
                deflate();
            }
        }

        firstBytesRead = false;
        super.close();
    }
}
