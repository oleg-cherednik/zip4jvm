package ru.olegcherednik.zip4jvm.model.src;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.math.NumberUtils;
import ru.olegcherednik.zip4jvm.exception.SplitPartNotFoundException;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * 7-Zip has not standard split algorithm. It creates the whole zip file first and then split it with required part size. It has following naming
 * convention:
 * <pre>
 * filename.zip.001
 * filename.zip.002
 * filename.zip.003
 * </pre>
 * According to the zip specification, this is not a split archive.
 *
 * @author Oleg Cherednik
 * @since 20.01.2020
 */
final class SevenZipSplitSrcZip extends SrcZip {

    public static boolean isCandidate(Path zip) {
        String ext = FilenameUtils.getExtension(zip.toString());
        return Files.isReadable(zip) && NumberUtils.isDigits(ext);
    }

    static SevenZipSplitSrcZip create(Path zip) {
        return new SevenZipSplitSrcZip(zip, createDisks(zip));
    }

    private static List<Disk> createDisks(Path zip) {
        int i = 1;
        long offs = 0;
        List<Disk> disks = new LinkedList<>();
        Path dir = zip.getParent();
        String baseName = FilenameUtils.getBaseName(zip.getFileName().toString());

        for (Path diskPath : getDiskPaths(dir, baseName + "\\.\\d+")) {
            String actualFileName = diskPath.getFileName().toString();
            String expectedFileName = String.format("%s.%03d", baseName, i);

            if (!actualFileName.equals(expectedFileName) || !Files.isReadable(diskPath))
                throw new SplitPartNotFoundException(dir.resolve(expectedFileName));

            long length = PathUtils.length(diskPath);
            disks.add(Disk.builder()
                          .pos(i)
                          .file(diskPath)
                          .offs(offs)
                          .length(length).build());
            offs += length;
            i++;
        }

        return disks;
    }

    private SevenZipSplitSrcZip(Path zip, List<Disk> disks) {
        super(zip, disks);
    }

}
