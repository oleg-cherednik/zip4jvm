package ru.olegcherednik.zip4jvm.io.in;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.out.SplitZipOutputStream;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
public class SplitZipInputStream extends BaseSplitZipInputStream {

    @Getter
    private final ZipModel zipModel;
    private final Path file;

    public SplitZipInputStream(ZipModel zipModel, long disk) throws IOException {
        super(new SingleZip(zipModel.getPartFile(disk)));
        file = zipModel.getZip().getPath();
        this.zipModel = zipModel;
        this.disk = disk;
        checkSignature();
    }

    private void checkSignature() throws IOException {
        if (disk == 0 && delegate.readSignature() != SplitZipOutputStream.SPLIT_SIGNATURE)
            throw new Zip4jvmException("Incorrect split file signature: " + file.getFileName());
    }

    @Override
    protected Path getNextDiskPath() {
//        return zipModel.getPartFile(++disk);
        return ZipModel.getSplitFilePath(file, ++disk);
    }

}
