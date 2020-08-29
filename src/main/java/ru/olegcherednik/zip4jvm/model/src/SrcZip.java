package ru.olegcherednik.zip4jvm.model.src;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang.ArrayUtils;

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
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * Represents either single solid zip file or split zip with multiple disks.
 *
 * @author Oleg Cherednik
 * @since 20.01.2020
 */
@Getter
public abstract class SrcZip {

    public static SrcZip of(Path zip) {
        if (SevenZipSplitSrcZip.isCandidate(zip))
            return SevenZipSplitSrcZip.create(zip);
        if (StandardSplitSrcZip.isCandidate(zip))
            return StandardSplitSrcZip.create(zip);
        return StandardSolidSrcZip.create(zip);
    }

    protected final Path path;
    @Getter(AccessLevel.NONE)
    protected final List<Disk> disks;
    protected final long length;

    protected SrcZip(Path path, List<Disk> disks) {
        this.path = path;
        this.disks = Collections.unmodifiableList(requireNotEmpty(disks, "SrcZip.disks"));
        length = disks.stream().mapToLong(Disk::getLength).sum();
    }

    public Disk getDiskByNo(int diskNo) {
        return disks.get(diskNo);
    }

    // TODO simplify for loop
    public Disk getDiskByAbsOffs(long absOffs) {
        for (SrcZip.Disk disk : disks) {
            if (disk.getAbsoluteOffs() + disk.getLength() <= absOffs && !isLast(disk))
                continue;

            return disk;
        }

        return disks.get(disks.size() - 1);
    }

    public boolean isSplit() {
        return disks.size() > 1;
    }

    public boolean isLast(Disk disk) {
        return requireNotNull(disk, "SrzZip.disk") == disks.get(disks.size() - 1);
    }

    protected static Set<Path> getDiskPaths(Path dir, String pattern) {
        FileFilter fileFilter = new RegexFileFilter(pattern);
        File[] files = dir.toFile().listFiles(fileFilter);

        return ArrayUtils.isEmpty(files) ? Collections.emptySet() : Arrays.stream(files)
                                                                          .map(File::toPath)
                                                                          .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", path.toString(), disks.size());
    }

    @Getter
    @Builder
    public static final class Disk {

        private final int no;
        private final Path file;
        /** Absolute offs of this disk starting from the beginning of the first disk */
        private final long absoluteOffs;
        private final long length;

        @Override
        public String toString() {
            return String.format("%s (offs: %s)", file, absoluteOffs);
        }
    }

}
