package ru.olegcherednik.zip4jvm.io.in;

import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.out.SplitZipOutputStream;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
public class SplitZipInputStream extends BaseZipModelDataInput {

    private long disk;

    public SplitZipInputStream(ZipModel zipModel, long disk) throws IOException {
        super(zipModel, zipModel.getPartFile(disk));
        this.disk = disk;
        checkSignature();
    }

    private void checkSignature() throws IOException {
        if (disk == 0 && delegate.readSignature() != SplitZipOutputStream.SPLIT_SIGNATURE)
            throw new Zip4jvmException("Incorrect split file signature: " + zipModel.getFile().getFileName());
    }

    @Override
    @SuppressWarnings("PMD.AvoidReassigningParameters")
    public int read(byte[] buf, int offs, final int len) throws IOException {
        int res = 0;
        int size = len;

        while (res < len) {
            int total = delegate.read(buf, offs, size);
            cycleBuffer.write(buf, offs, size);

            if (total > 0)
                res += total;

            if (total == IOUtils.EOF || total < size) {
                openNextDisk();
                offs += Math.max(0, total);
                size -= Math.max(0, total);
            }
        }

        return res;
    }

    @Override
    public void skip(long bytes) throws IOException {
        while (bytes > 0) {
            int expected = (int)Math.min(bytes, Integer.MAX_VALUE);
            int actual = delegate.skip(expected);

            bytes -= actual;

            if (actual < expected)
                openNextDisk();
        }
    }

    private void openNextDisk() throws IOException {
        Path splitFile = zipModel.getPartFile(++disk);
        delegate.close();
        delegate = new LittleEndianReadFile(splitFile);
    }

}
