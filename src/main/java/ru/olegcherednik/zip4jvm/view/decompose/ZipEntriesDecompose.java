package ru.olegcherednik.zip4jvm.view.decompose;

import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.entry.ZipEntriesView;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
public final class ZipEntriesDecompose implements Decompose {

    private final BlockModel blockModel;
    private final ZipInfoSettings settings;

    public ZipEntriesDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        this.blockModel = blockModel;
        this.settings = settings;
    }

    @Override
    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        if (blockModel.isEmpty())
            return false;

        emptyLine |= zipEntriesView().print(out, emptyLine);
        return localFileHeaderDecompose().printTextInfo(out, emptyLine);
    }

    @Override
    public void decompose(Path dir) throws IOException {
        if (blockModel.isEmpty())
            return;

        dir = Files.createDirectories(dir.resolve("entries"));
        localFileHeaderDecompose().decompose(dir);
    }

    private ZipEntriesView zipEntriesView() {
        long totalEntries = blockModel.getFileNameZipEntryBlock().size();
        return new ZipEntriesView(totalEntries, settings.getOffs(), settings.getColumnWidth());
    }

    private LocalFileHeaderDecompose localFileHeaderDecompose() {
        return new LocalFileHeaderDecompose(blockModel, settings);
    }

}
