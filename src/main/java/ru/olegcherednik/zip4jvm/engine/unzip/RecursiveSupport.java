package ru.olegcherednik.zip4jvm.engine.unzip;

import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.BiConsumer;

/**
 * @author Oleg Cherednik
 * @since 14.09.2025
 */
final class RecursiveSupport implements BiConsumer<Path, ZipEntry> {

    private final Queue<SrcZip> nextLevelQueue = new LinkedList<>();

    public boolean isEmpty() {
        return nextLevelQueue.isEmpty();
    }

    public SrcZip next() {
        return nextLevelQueue.remove();
    }


    // ---------- Consumer ----------

    @Override
    public void accept(Path dstDir, ZipEntry zipEntry) {
        if (!zipEntry.isRegularFile())
            return;
        if (!zipEntry.getFileName().endsWith(".zip"))
            return;
        nextLevelQueue.add(SrcZip.of(dstDir.resolve(zipEntry.getFileName())));
    }

}
