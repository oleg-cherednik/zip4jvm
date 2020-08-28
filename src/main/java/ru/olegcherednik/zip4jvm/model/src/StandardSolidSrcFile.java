package ru.olegcherednik.zip4jvm.model.src;

import java.nio.file.Path;
import java.util.Collections;

/**
 * @author Oleg Cherednik
 * @since 20.01.2020
 */
final class StandardSolidSrcFile extends SrcFile {

    public static StandardSolidSrcFile create(Path file) {
        return new StandardSolidSrcFile(file);
    }

    private StandardSolidSrcFile(Path path) {
        super(path, Collections.singletonList(Item.create(path)));
    }

    @Override
    public String toString() {
        return path.toString();
    }

}
