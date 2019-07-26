package com.cop.zip4j.io.entry;

import com.cop.zip4j.io.ZipOutputStream;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.utils.InternalZipConstants;
import lombok.NonNull;

import java.io.IOException;
import java.util.zip.Deflater;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
public class DeflateEntryOutputDelegate extends CommonEntryOutputDelegate {

    private final byte[] buf = new byte[InternalZipConstants.BUFF_SIZE];
    private final Deflater deflater = new Deflater();

    public boolean firstBytesRead;

    public DeflateEntryOutputDelegate(@NonNull ZipOutputStream zipOutputStream, @NonNull CompressionLevel compressionLevel) {
        super(zipOutputStream);
        deflater.setLevel(compressionLevel.getValue());
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        crc32.update(buf, offs, len);
        totalBytesRead += len;

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
