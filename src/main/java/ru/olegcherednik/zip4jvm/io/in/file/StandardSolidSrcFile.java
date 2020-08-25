package ru.olegcherednik.zip4jvm.io.in.file;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

/**
 * @author Oleg Cherednik
 * @since 20.01.2020
 */
class StandardSolidSrcFile extends SrcFile {

    public static StandardSolidSrcFile create(Path file) {
        return new StandardSolidSrcFile(file);
    }

    private StandardSolidSrcFile(Path path) {
        super(path, Collections.singletonList(Item.create(path)));
    }

    @Override
    public DataInputFile dataInputFile() throws IOException {
        return new LittleEndianReadFile(path);
    }

    @Override
    public boolean isSplit() {
        return false;
    }

    @Override
    public String toString() {
        return path.toString();
    }

}
