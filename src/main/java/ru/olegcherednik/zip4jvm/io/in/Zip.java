package ru.olegcherednik.zip4jvm.io.in;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireExists;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireRegularFile;

/**
 * @author Oleg Cherednik
 * @since 20.01.2020
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Zip {

    public static Zip of(Path file) {
        Zip zip = SevenZipSplitZip.create(file);

        if (zip != null)
            return zip;

        requireExists(file);
        requireRegularFile(file, "Zip.file");
        return new StandardZip(file);
    }

    public abstract Path getPath();

    public abstract Path getDiskFile(int disk);

    public abstract long getTotalDisks();

    public abstract DataInputFile openDataInputFile() throws IOException;

    public abstract boolean isSplit() throws IOException;

}
