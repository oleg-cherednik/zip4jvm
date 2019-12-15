package ru.olegcherednik.zip4jvm.io.in.ng;

import org.apache.commons.lang.NotImplementedException;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.out.SplitZipOutputStream;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 27.10.2019
 */
public class SplitZipInputStream extends BaseZipInputStream {

    private final ZipModel zipModel;
    private long disk;

    public SplitZipInputStream(ZipModel zipModel, long disk) throws IOException {
        super(new FileInputStream(zipModel.getPartFile(disk).toFile()));
        this.zipModel = zipModel;
        this.disk = disk;
        checkSignature();
    }

    private void checkSignature() throws IOException {
        if (disk != 0)
            return;

        // TODO duplication with LittleEndianReadFile
        int b0 = in.read();
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        int signature = b3 << 24 | b2 << 16 | b1 << 8 | b0;

        if (signature != SplitZipOutputStream.SPLIT_SIGNATURE)
            throw new Zip4jvmException("Incorrect split file signature: " + zipModel.getFile().getFileName());
    }

    @Override
    public int read() throws IOException {
        throw new NotImplementedException();
    }
}
