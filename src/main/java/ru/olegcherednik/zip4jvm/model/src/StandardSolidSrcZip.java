package ru.olegcherednik.zip4jvm.model.src;

import ru.olegcherednik.zip4jvm.utils.PathUtils;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 20.01.2020
 */
final class StandardSolidSrcZip extends SrcZip {

    public static StandardSolidSrcZip create(Path zip) {
        return new StandardSolidSrcZip(zip);
    }

    private StandardSolidSrcZip(Path zip) {
        super(zip, createDisks(zip));
    }

    private static List<Disk> createDisks(Path zip) {
        Disk disk = Disk.builder()
                        .pos(0)
                        .file(zip)
                        .absoluteOffs(0)
                        .length(PathUtils.length(zip)).build();
        return Collections.singletonList(disk);
    }

    @Override
    public String toString() {
        return path.toString();
    }

}
