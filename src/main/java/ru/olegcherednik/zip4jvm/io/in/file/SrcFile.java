package ru.olegcherednik.zip4jvm.io.in.file;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 20.01.2020
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class SrcFile {

    public static SrcFile of(Path file) {
        SrcFile srcFile = SevenZipSplitSrcFile.create(file);
        return srcFile == null ? new StandardSrcFile(file) : srcFile;
    }

    public abstract Path getPath();

    public abstract Path getDiskFile(int disk);

    public abstract long getTotalDisks();

    public abstract DataInputFile dataInputFile() throws IOException;

    public abstract boolean isSplit() throws IOException;

}
