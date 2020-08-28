package ru.olegcherednik.zip4jvm.model.src;

import org.apache.commons.io.FilenameUtils;
import ru.olegcherednik.zip4jvm.exception.SplitPartNotFoundException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.ZipInputStream;
import ru.olegcherednik.zip4jvm.io.readers.BaseZipModelReader;
import ru.olegcherednik.zip4jvm.io.readers.EndCentralDirectoryReader;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

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
        int i = 1;
        long absOffs = 0;
        List<Disk> disks = new LinkedList<>();
        Path dir = zip.getParent();
        String baseName = FilenameUtils.getBaseName(zip.getFileName().toString());

        for (Path diskPath : getDiskPaths(dir, baseName + "\\.z\\d+")) {
            String actualFileName = diskPath.getFileName().toString();
            String expectedFileName = String.format("%s.z%02d", baseName, i);

            if (!actualFileName.equals(expectedFileName) || !Files.isReadable(diskPath))
                throw new SplitPartNotFoundException(dir.resolve(expectedFileName));

            long length = PathUtils.length(diskPath);
            disks.add(Disk.builder()
                          .pos(i)
                          .file(diskPath)
                          .offs(absOffs)
                          .length(length).build());
            absOffs += length;
            i++;
        }

        if (i == getTotalDisks(zip))
            throw new SplitPartNotFoundException(dir.resolve(String.format("%s.%02d", baseName, i)));

        disks.add(Disk.builder()
                      .pos(i)
                      .file(zip)
                      .offs(absOffs)
                      .length(PathUtils.length(zip)).build());

        return disks;
    }

    private static int getTotalDisks(Path zip) {
        try (DataInput in = new ZipInputStream(StandardSolidSrcZip.create(zip))) {
            BaseZipModelReader.findCentralDirectorySignature(in);
            return new EndCentralDirectoryReader(Charsets.UNMODIFIED).read(in).getTotalDisks();
        } catch(Exception e) {
            return 0;
        }
    }

    private StandardSplitSrcZip(Path zip, List<Disk> disks) {
        super(zip, disks);
    }

    @Override
    public Disk getMainDisk() {
        return disks.get(disks.size() - 1);
    }

}
