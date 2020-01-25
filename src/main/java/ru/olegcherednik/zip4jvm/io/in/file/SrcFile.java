package ru.olegcherednik.zip4jvm.io.in.file;

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
public abstract class SrcFile {

    public static SrcFile of(Path file) {
        SrcFile srcFile = SevenZipSplitSrcFile.create(file);

        if (srcFile != null)
            return srcFile;

        requireExists(file);
        requireRegularFile(file, "Zip.file");
        return new StandardSrcFile(file);
    }

    public abstract Path getPath();

    public abstract Path getDiskFile(int disk);

    public abstract long getTotalDisks();

    public abstract DataInputFile dataInputFile() throws IOException;

    public abstract boolean isSplit() throws IOException;

}
