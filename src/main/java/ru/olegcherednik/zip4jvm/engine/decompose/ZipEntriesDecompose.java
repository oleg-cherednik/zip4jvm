package ru.olegcherednik.zip4jvm.engine.decompose;

import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.entry.ZipEntryListView;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
final class ZipEntriesDecompose {

    private final BlockModel blockModel;
    private final ZipModel zipModel;
    private final ZipInfoSettings settings;

    public ZipEntriesDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        this.blockModel = blockModel;
        zipModel = blockModel.getZipModel();
        this.settings = settings;
    }

    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        if (blockModel.getZipEntryModel() == null)
            return false;

        emptyLine |= createView().print(out, emptyLine);
        return localFileHeaderDecompose().printTextInfo(out, emptyLine);
    }

    public void write(Path dir) throws IOException {
        if (blockModel.getZipEntryModel() == null)
            return;

        dir = Files.createDirectories(dir.resolve("entries"));
        localFileHeaderDecompose().write(dir);
    }

    private ZipEntryListView createView() {
        return ZipEntryListView.builder()
                               .blockZipEntryModel(blockModel.getZipEntryModel())
                               .getDataFunc(Utils.getDataFunc(blockModel.getZipModel()))
                               .charset(settings.getCharset())
                               .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

    private LocalFileHeaderDecompose localFileHeaderDecompose() {
        return new LocalFileHeaderDecompose(blockModel, settings);
    }

}
