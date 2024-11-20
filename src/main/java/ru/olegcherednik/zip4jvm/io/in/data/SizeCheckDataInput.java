package ru.olegcherednik.zip4jvm.io.in.data;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.xxx.XxxBaseDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.xxx.XxxDataInput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 17.11.2024
 */
public class SizeCheckDataInput extends XxxBaseDataInput {

    private final long expectedSize;
    private final String fileName;
    private long size;

    public static SizeCheckDataInput uncompressedSize(ZipEntry zipEntry, XxxDataInput in) {
        return new SizeCheckDataInput(zipEntry.getUncompressedSize(), zipEntry.getFileName(), in);
    }

    protected SizeCheckDataInput(long expectedSize, String fileName, XxxDataInput in) {
        super(in);
        this.expectedSize = Math.max(0, expectedSize);
        this.fileName = fileName;
    }

    // ---------- ReadBuffer ----------

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        int readNow = super.read(buf, offs, len);

        if (readNow != IOUtils.EOF)
            size += readNow;

        return readNow;
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        if (size != expectedSize)
            throw new Zip4jvmException("UncompressedSize is not matched: " + fileName);

        super.close();
    }

}
