package com.cop.zip4j.io.delegate;

import com.cop.zip4j.io.CipherOutputStream;
import com.cop.zip4j.io.DeflateOutputStream;
import com.cop.zip4j.model.ZipParameters;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.NonNull;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
public class DeflateOutputDelegate extends CommonOutputDelegate {

    public DeflateOutputDelegate(CipherOutputStream cipherOutputStream) {
        super(cipherOutputStream);
    }

    @Override
    public void putNextEntry(@NonNull PathZipEntry entry, @NonNull ZipParameters parameters) {
        super.putNextEntry(entry, parameters);
        ((DeflateOutputStream)cipherOutputStream).deflater.reset();
        ((DeflateOutputStream)cipherOutputStream).deflater.setLevel(parameters.getCompressionLevel().getValue());
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        cipherOutputStream.crc.update(buf, offs, len);
        cipherOutputStream.totalBytesRead += len;

        ((DeflateOutputStream)cipherOutputStream).deflater.setInput(buf, offs, len);

        while (!((DeflateOutputStream)cipherOutputStream).deflater.needsInput()) {
            deflate();
        }
    }

    private void deflate() throws IOException {
        int len = ((DeflateOutputStream)cipherOutputStream).deflater.deflate(((DeflateOutputStream)cipherOutputStream).buf, 0,
                ((DeflateOutputStream)cipherOutputStream).buf.length);

        if (len <= 0)
            return;

        if (((DeflateOutputStream)cipherOutputStream).deflater.finished()) {
            if (len == 4)
                return;
            if (len < 4)
                return;
            len -= 4;
        }

        if (((DeflateOutputStream)cipherOutputStream).firstBytesRead)
            _write(((DeflateOutputStream)cipherOutputStream).buf, 0, len);
        else {
            _write(((DeflateOutputStream)cipherOutputStream).buf, 2, len - 2);
            ((DeflateOutputStream)cipherOutputStream).firstBytesRead = true;
        }
    }

    @Override
    public void closeEntry() throws IOException {
        if (!((DeflateOutputStream)cipherOutputStream).deflater.finished()) {
            ((DeflateOutputStream)cipherOutputStream).deflater.finish();

            while (!((DeflateOutputStream)cipherOutputStream).deflater.finished()) {
                deflate();
            }
        }

        ((DeflateOutputStream)cipherOutputStream).firstBytesRead = false;
        super.closeEntry();
    }
}
