package net.lingala.zip4j.io;

import net.lingala.zip4j.model.ZipModel;

import java.io.IOException;
import java.io.OutputStream;

public class ZipOutputStream extends DeflateOutputStream {

    public ZipOutputStream(OutputStream outputStream) {
        this(outputStream, null);
    }

    public ZipOutputStream(OutputStream outputStream, ZipModel zipModel) {
        super(outputStream, zipModel);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        crc.update(b, off, len);
        updateTotalBytesRead(len);
        super.write(b, off, len);
    }
}
