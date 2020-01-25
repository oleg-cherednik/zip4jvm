package ru.olegcherednik.zip4jvm.io.in;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 20.01.2020
 */
@Getter
@RequiredArgsConstructor
public final class StandardZip extends Zip {

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
    public DataInputFile openDataInputFile() throws IOException {
        return new LittleEndianReadFile(path);
    }

    @Override
    public String toString() {
        return path.toString();
    }

    public static Path getDiskFile(Path file, long disk) {
        return file.getParent().resolve(String.format("%s.z%02d", FilenameUtils.getBaseName(file.toString()), disk));
    }
}
