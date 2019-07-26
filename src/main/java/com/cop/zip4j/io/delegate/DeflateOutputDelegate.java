package com.cop.zip4j.io.delegate;

import com.cop.zip4j.io.CipherOutputStream;
import com.cop.zip4j.model.ZipParameters;
import com.cop.zip4j.model.entry.PathZipEntry;
import com.cop.zip4j.utils.InternalZipConstants;
import lombok.NonNull;

import java.io.IOException;
import java.util.zip.Deflater;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
public class DeflateOutputDelegate extends CommonOutputDelegate {

    public final byte[] buf = new byte[InternalZipConstants.BUFF_SIZE];
    public final Deflater deflater = new Deflater();

    public boolean firstBytesRead;

    public DeflateOutputDelegate(CipherOutputStream cipherOutputStream) {
        super(cipherOutputStream);
    }

    @Override
    public void putNextEntry(@NonNull PathZipEntry entry, @NonNull ZipParameters parameters) {
        super.putNextEntry(entry, parameters);
        deflater.reset();
        deflater.setLevel(parameters.getCompressionLevel().getValue());
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        cipherOutputStream.crc.update(buf, offs, len);
        cipherOutputStream.totalBytesRead += len;

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
    public void closeEntry() throws IOException {
        if (!deflater.finished()) {
            deflater.finish();

            while (!deflater.finished()) {
                deflate();
            }
        }

        firstBytesRead = false;
        super.closeEntry();
    }
}
