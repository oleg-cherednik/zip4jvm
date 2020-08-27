package ru.olegcherednik.zip4jvm.io.in.file;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Oleg Cherednik
 * @since 20.01.2020
 */
@Getter
public abstract class SrcFile {

    public static SrcFile of(Path file) {
        if (SevenZipSplitSrcFile.isCandidate(file))
            return SevenZipSplitSrcFile.create(file);
        if (StandardSplitSrcFile.isCandidate(file))
            return StandardSplitSrcFile.create(file);
        return StandardSolidSrcFile.create(file);
    }

    protected final Path path;
    protected final List<Item> items;
    protected final long length;

    protected SrcFile(Path path, List<Item> items) {
        this.path = path;
        this.items = CollectionUtils.isEmpty(items) ? Collections.emptyList() : Collections.unmodifiableList(items);
        length = items.stream().mapToLong(Item::getLength).sum();
    }

    public Item getDisk(int disk) {
        if (items.isEmpty())
            return null;
        if (disk == 0)
            return items.get(items.size() - 1);
        return disk <= items.size() ? items.get(disk - 1) : null;
    }

    public abstract DataInputFile dataInputFile() throws IOException;

    public boolean isSplit() {
        return items.size() > 1;
    }

    public boolean isLast(Item item) {
        return item == null || items.get(items.size() - 1) == item;
    }

    protected static Set<Path> getParts(Path dir, String pattern) {
        FileFilter fileFilter = new RegexFileFilter(pattern);
        File[] files = dir.toFile().listFiles(fileFilter);

        if (ArrayUtils.isEmpty(files))
            return Collections.emptySet();

        Set<Path> parts = new TreeSet<>();

        for (File file : files)
            parts.add(file.toPath());

        return parts;
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", path.toString(), items.size());
    }

    @Getter
    @Builder
    public static final class Item {

        public static Item create(Path path) {
            return builder()
                    .pos(0)
                    .file(path)
                    .offs(0)
                    .length(PathUtils.length(path)).build();
        }

        private final int pos;
        private final Path file;
        private final long offs;
        private final long length;

        @Override
        public String toString() {
            return String.format("%s (offs: %s)", file, offs);
        }
    }

}
