package ru.olegcherednik.zip4jvm.io.in.data;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.io.in.file.Zip;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.IOException;
import java.nio.file.Path;

/**
 * TODO 7-Zip is should be merged with base
 *
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
public class SplitZipInputStream extends BaseSplitZipInputStream {

    @Getter
    private final ZipModel zipModel;
    private final Path file;

    public SplitZipInputStream(ZipModel zipModel, long disk) throws IOException {
        super(Zip.of(zipModel.getPartFile(disk)));
        file = zipModel.getZip().getPath();
        this.zipModel = zipModel;
        this.disk = disk;
        // TODO should be enabled
//        checkSignature();
    }

//    private void checkSignature() throws IOException {
//        if (disk == 0 && delegate.readSignature() != SplitZipOutputStream.SPLIT_SIGNATURE)
//            throw new Zip4jvmException("Incorrect split file signature: " + file.getFileName());
//    }

    @Override
    protected Path getNextDiskPath() {
//        return zipModel.getPartFile(++disk);
        return ZipModel.getDiskFile(file, ++disk);
    }

}
