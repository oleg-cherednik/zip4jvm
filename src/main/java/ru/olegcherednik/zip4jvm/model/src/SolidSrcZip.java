package ru.olegcherednik.zip4jvm.model.src;

import ru.olegcherednik.zip4jvm.utils.PathUtils;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 20.01.2020
 */
final class SolidSrcZip extends SrcZip {

    public static SolidSrcZip create(Path zip) {
        return new SolidSrcZip(zip);
    }

    private SolidSrcZip(Path zip) {
        super(zip, createDisks(zip));
    }

    private static List<Disk> createDisks(Path zip) {
        Disk disk = Disk.builder()
                        .no(0)
                        .path(zip)
                        .absoluteOffs(0)
                        .size(PathUtils.size(zip))
                        .last(true).build();

        return Collections.singletonList(disk);
    }

    @Override
    public String toString() {
        return path.toString();
    }

}
