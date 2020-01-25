package ru.olegcherednik.zip4jvm.io.in;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.out.SplitZipOutputStream;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 20.01.2020
 */
@Getter
@RequiredArgsConstructor
final class StandardZip extends Zip {

    private final Path path;

    @Override
    public Path getDiskFile(int disk) {
        return path;
    }

    @Override
    public long getTotalDisks() {
        return 0;
    }

    @Override
    public DataInputFile dataInputFile() throws IOException {
        return new LittleEndianReadFile(path);
    }

    @Override
    public boolean isSplit() throws IOException {
        try (DataInput in = new SingleZipInputStream(ZipModel.getDiskFile(path, 1))) {
            return in.readDwordSignature() == SplitZipOutputStream.SPLIT_SIGNATURE;
        }
    }

    @Override
    public String toString() {
        return path.toString();
    }

}
