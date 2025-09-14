package ru.olegcherednik.zip4jvm.engine.unzip;

import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.BiConsumer;

/**
 * @author Oleg Cherednik
 * @since 14.09.2025
 */
@RequiredArgsConstructor
final class RecursiveSupport implements BiConsumer<Path, ZipEntry> {

    private final int recursiveLevel;
    private final Queue<SrcZip> zipQueue = new LinkedList<>();
    @Setter
    private Path rootPath;

    public boolean isEmpty() {
        return zipQueue.isEmpty();
    }

    public SrcZip next() {
        return zipQueue.remove();
    }

    private static int getLevel(Path path) {
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
        if (!zipEntry.isRegularFile())
            return;
        if (!zipEntry.getFileName().endsWith(".zip"))
            return;
        if (recursiveLevel == UnzipSettings.RECURSIVE_LEVEL_OFF)
            return;

        Path zip = dstDir.resolve(zipEntry.getFileName());
        Path zipRelative = rootPath.relativize(zip);
        int level = getLevel(zipRelative);

        if (recursiveLevel == UnzipSettings.RECURSIVE_LEVEL_MAX || level <= recursiveLevel) {
            System.out.println(level + " - " + zipRelative);
            zipQueue.add(SrcZip.of(dstDir.resolve(zipEntry.getFileName())));
        }
    }

}
