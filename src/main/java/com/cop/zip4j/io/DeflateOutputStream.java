package com.cop.zip4j.io;

import com.cop.zip4j.exception.ZipException;
import com.cop.zip4j.model.CompressionMethod;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.ZipParameters;
import com.cop.zip4j.utils.InternalZipConstants;
import lombok.NonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.Deflater;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public class DeflateOutputStream extends CipherOutputStream {

    private final byte[] buf = new byte[InternalZipConstants.BUFF_SIZE];
    private final Deflater deflater = new Deflater();

    private boolean firstBytesRead;

    public DeflateOutputStream(@NonNull SplitOutputStream out, @NonNull ZipModel zipModel) {
        super(out, zipModel);
    }

    @Override
    protected void putNextEntry(Path file, String fileNameStream, ZipParameters parameters) {
        super.putNextEntry(file, fileNameStream, parameters);

        if (parameters.getCompressionMethod() != CompressionMethod.DEFLATE)
            return;

        deflater.reset();
        deflater.setLevel(parameters.getCompressionLevel().getValue());
    }

    private void deflate() throws IOException {
        int len = deflater.deflate(buf, 0, buf.length);
        if (len > 0) {
            if (deflater.finished()) {
                if (len == 4)
                    return;
                if (len < 4)
                    return;
                len -= 4;
            }
            if (!firstBytesRead) {
                super.write(buf, 2, len - 2);
                firstBytesRead = true;
            } else {
                super.write(buf, 0, len);
            }
        }
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        crc.update(buf, offs, len);
        totalBytesRead += len;

        if (parameters.getCompressionMethod() != CompressionMethod.DEFLATE)
            super.write(buf, offs, len);
        else {
            deflater.setInput(buf, offs, len);
            while (!deflater.needsInput()) {
                deflate();
            }
        }
    }

    @Override
    public void closeEntry() throws IOException, ZipException {
        if (parameters.getCompressionMethod() == CompressionMethod.DEFLATE) {
            if (!deflater.finished()) {
                deflater.finish();

                while (!deflater.finished()) {
                    deflate();
                }
            }

            firstBytesRead = false;
        }
        super.closeEntry();
    }

}
