package ru.olegcherednik.zip4jvm.model.src;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotEmpty;

/**
 * Represents either single solid zip file or split zip with multiple disks. This class used to check that required disks are available, otherwise
 * given {@code zip} file it treats as {@link SolidSrcZip} and this file could be not a zip file at all (to check this {@link ZipModel} should be
 * built).
 *
 * @author Oleg Cherednik
 * @since 20.01.2020
 */
@Getter
public abstract class SrcZip {

    protected final Path path;
    @Getter(AccessLevel.NONE)
    protected final List<Disk> disks;
    protected final long size;
    protected final long splitSize;

    public static SrcZip of(Path zip) {
        if (SevenZipSplitSrcZip.isCandidate(zip))
            return SevenZipSplitSrcZip.create(zip);
        if (PkwareSplitSrcZip.isCandidate(zip))
            return PkwareSplitSrcZip.create(zip);
        return SolidSrcZip.create(zip);
    }

    protected SrcZip(Path path, List<Disk> disks) {
        this.path = path;
        this.disks = Collections.unmodifiableList(requireNotEmpty(disks, "SrcZip.disks"));
        size = calcSize(disks);
        splitSize = calcSplitSize(disks);
    }

    private static long calcSize(List<Disk> disks) {
        return disks.stream().mapToLong(Disk::getSize).sum();
    }

    private static long calcSplitSize(List<Disk> disks) {
        return disks.size() == 1 ? ZipModel.NO_SPLIT : disks.stream().mapToLong(Disk::getSize).max().orElse(ZipModel.NO_SPLIT);
    }

    public int getTotalDisks() {
        return disks.size();
    }

    public Disk getDiskByNo(int diskNo) {
        return disks.get(diskNo);
    }

    public Disk getDiskByAbsoluteOffs(long absoluteOffs) {
        for (SrcZip.Disk disk : disks)
            if (absoluteOffs - disk.getAbsoluteOffs() <= disk.getSize())
                return disk;

        return getLastDisk();
    }

    private Disk getLastDisk() {
        return disks.get(disks.size() - 1);
    }

    protected static Set<Path> getDiskPaths(Path dir, String pattern) {
        FileFilter fileFilter = new RegexFileFilter(pattern);
        File[] files = dir.toFile().listFiles(fileFilter);

        return ArrayUtils.isEmpty(files) ? Collections.emptySet() : Arrays.stream(files)
                                                                          .map(File::toPath)
                                                                          .collect(Collectors.toCollection(TreeSet::new));
    }

    public Path getDiskPath(int diskNo) {
        return getDiskPath(path, diskNo);
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", path.toString(), disks.size());
    }

    public static Path getDiskPath(Path path, int diskNo) {
        Path dir = path.getParent();
        String baseName = FilenameUtils.getBaseName(path.toString());
        return dir.resolve(String.format("%s.z%02d", baseName, diskNo));
    }

    @Getter
    @Builder
    public static final class Disk {

        private final int no;
        private final Path path;
        /** Absolute offs of this disk starting from the beginning of the first disk */
        private final long absoluteOffs;
        private final long size;
        private final boolean last;

        public String getFileName() {
            return path.getFileName().toString();
        }

        @Override
        public String toString() {
            return String.format("%s (offs: %s)", path, absoluteOffs);
        }
    }

}
