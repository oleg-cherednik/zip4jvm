package ru.olegcherednik.zip4jvm.engine.unzip;

import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

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


    // ---------- Consumer ----------

    @Override
    public void accept(Path dstDir, ZipEntry zipEntry) {
        System.out.println(dstDir + "/" + zipEntry.getFileName());

        if (!zipEntry.isRegularFile())
            return;
        if (!zipEntry.getFileName().endsWith(".zip"))
            return;
        zipQueue.add(SrcZip.of(dstDir.resolve(zipEntry.getFileName())));
    }

}
