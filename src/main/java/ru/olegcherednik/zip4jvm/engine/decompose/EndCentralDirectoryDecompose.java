package ru.olegcherednik.zip4jvm.engine.decompose;

import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.EndCentralDirectoryView;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
final class EndCentralDirectoryDecompose {

    private static final String FILE_NAME = "end_central_directory";

    private final ZipModel zipModel;
    private final EndCentralDirectory endCentralDirectory;
    private final Block block;
    private final ZipInfoSettings settings;

    public EndCentralDirectoryDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        zipModel = blockModel.getZipModel();
        endCentralDirectory = blockModel.getEndCentralDirectory();
        block = blockModel.getEndCentralDirectoryBlock();
        this.settings = settings;
    }

    public boolean print(PrintStream out, boolean emptyLine) {
        return createView().print(out, emptyLine);
    }

    public void write(Path dir) throws IOException {
        Utils.print(dir.resolve(FILE_NAME + ".txt"), out -> createView().print(out));
        Utils.copyLarge(zipModel, dir.resolve(FILE_NAME + ".data"), block);
    }

    private EndCentralDirectoryView createView() {
        return EndCentralDirectoryView.builder()
                                      .endCentralDirectory(endCentralDirectory)
                                      .block(block)
                                      .charset(settings.getCharset())
                                      .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

}
