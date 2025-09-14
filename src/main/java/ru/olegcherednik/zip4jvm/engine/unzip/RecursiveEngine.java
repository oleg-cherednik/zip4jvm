package ru.olegcherednik.zip4jvm.engine.unzip;

import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.function.BiConsumer;

/**
 * @author Oleg Cherednik
 * @since 14.09.2025
 */
@RequiredArgsConstructor
final class RecursiveEngine implements BiConsumer<Path, ZipEntry>, Iterator<SrcZip> {

    private final int recursiveLevel;
    private final Queue<SrcZip> zipQueue = new LinkedList<>();
    @Setter
    private Path rootPath;

    public boolean isEmpty() {
        return zipQueue.isEmpty();
    }

    // ---------- Iterator ----------

    @Override
    public boolean hasNext() {
        return !zipQueue.isEmpty();
    }

    @Override
    public SrcZip next() {
        if (!hasNext())
            throw new NoSuchElementException("RecursiveEngine.zipQueue");
        return zipQueue.remove();
    }

    // ---------- static ----------

    private static int getRecursiveLevel(Path path) {
        int level = 1;
        String str = path.toString();

        // start from '1' to exclude '/' at the beginning
        for (int i = 1; i < str.length(); i++) {
            char ch = str.charAt(i);

            if (ch == PathUtils.SLASH || ch == PathUtils.BACK_SLASH)
                level++;
        }

        return level;
    }

    // ---------- Consumer ----------

    @Override
    public void accept(Path dstDir, ZipEntry zipEntry) {
        if (recursiveLevel == UnzipSettings.RECURSIVE_LEVEL_OFF)
            return;
        if (!zipEntry.isRegularFile())
            return;
        if (!zipEntry.getFileName().endsWith(".zip"))
            return;

        Path zip = dstDir.resolve(zipEntry.getFileName());
        int recursiveLevel = getRecursiveLevel(rootPath.relativize(zip));

        if (this.recursiveLevel == UnzipSettings.RECURSIVE_LEVEL_MAX || recursiveLevel <= this.recursiveLevel)
            zipQueue.add(SrcZip.of(zip));
    }

}
