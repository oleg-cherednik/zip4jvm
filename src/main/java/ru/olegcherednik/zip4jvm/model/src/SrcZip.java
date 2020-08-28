package ru.olegcherednik.zip4jvm.model.src;

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
    protected final List<Disk> disks;
    protected final long length;

    protected SrcZip(Path path, List<Disk> disks) {
        this.path = path;
        this.disks = Collections.unmodifiableList(requireNotEmpty(disks, "SrcZip.disks"));
        length = disks.stream().mapToLong(Disk::getLength).sum();
    }

    public Disk getDisk(int disk) {
        if (disks.isEmpty())
            return null;
        if (disk == 0)
            return disks.get(disks.size() - 1);
        return disk <= disks.size() ? disks.get(disk - 1) : null;
    }

    public boolean isSplit() {
        return disks.size() > 1;
    }

    public boolean isLast(Disk disk) {
        return disk == null || disks.get(disks.size() - 1) == disk;
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

        private final int pos;
        private final Path file;
        /** Absolute offs of this disk starting from the beginning of the first disk */
        private final long absOffs;
        private final long length;

        @Override
        public String toString() {
            return String.format("%s (offs: %s)", file, absOffs);
        }
    }

}
