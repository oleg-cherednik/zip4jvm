package ru.olegcherednik.zip4jvm.model.src;

import org.apache.commons.io.FilenameUtils;
import ru.olegcherednik.zip4jvm.exception.SplitPartNotFoundException;
import ru.olegcherednik.zip4jvm.io.readers.ZipModelReader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 25.08.2020
 */
final class StandardSplitSrcZip extends SrcZip {

    public static boolean isCandidate(Path zip) {
        return Files.isReadable(zip) && getTotalDisks(zip) > 0;
    }

    public static StandardSplitSrcZip create(Path zip) {
        return new StandardSplitSrcZip(zip, createDisks(zip));
    }

    private static List<Disk> createDisks(Path zip) {
        int i = 0;
        long absoluteOffs = 0;
        List<Disk> disks = new LinkedList<>();
        Path dir = zip.getParent();
        String baseName = FilenameUtils.getBaseName(zip.getFileName().toString());
        int totalDisk = getTotalDisks(zip);

        for (Path diskPath : getDiskPaths(dir, baseName + "\\.(?:z\\d+|zip)")) {
            String actualFileName = diskPath.getFileName().toString();
            String expectedFileName = i == totalDisk ? baseName + ".zip" : String.format("%s.z%02d", baseName, i + 1);

            if (!actualFileName.equals(expectedFileName) || !Files.isReadable(diskPath))
                throw new SplitPartNotFoundException(dir.resolve(expectedFileName));

            Disk disk = Disk.builder()
                            .no(i)
                            .file(diskPath)
                            .absoluteOffs(absoluteOffs)
                            .length(PathUtils.length(diskPath)).build();

            disks.add(disk);
            absoluteOffs += disk.getLength();
            i++;
        }

        return disks;
    }

    private static int getTotalDisks(Path zip) {
        try {
            // TODO ready only required parts
            ZipModel model = new ZipModelReader(SolidSrcZip.create(zip)).read();
            return model.getTotalDisks();
        } catch(IOException e) {
            return 0;
        }
    }

    private StandardSplitSrcZip(Path zip, List<Disk> disks) {
        super(zip, disks);
    }

}
