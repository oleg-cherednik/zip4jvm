package ru.olegcherednik.zip4jvm.io.in.file;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 20.01.2020
 */
@Getter
public abstract class SrcFile {

    public static SrcFile of(Path file) {
        SrcFile srcFile = SevenZipSplitSrcFile.create(file);
        srcFile = srcFile == null ? StandardSplitSrcFile.create(file) : srcFile;
        return srcFile == null ? new StandardSrcFile(file) : srcFile;
    }

    protected final Path path;
    protected final List<Item> items;

    protected SrcFile(Path path, List<Item> items) {
        this.path = path;
        this.items = CollectionUtils.isEmpty(items) ? Collections.emptyList() : Collections.unmodifiableList(items);
    }

    public Item getDisk(int disk) {
        if (items.isEmpty())
            return null;
        if (disk == 0)
            return items.get(items.size() - 1);
        return disk < items.size() ? items.get(disk) : null;
    }

    public abstract DataInputFile dataInputFile() throws IOException;

    public abstract boolean isSplit() throws IOException;

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
