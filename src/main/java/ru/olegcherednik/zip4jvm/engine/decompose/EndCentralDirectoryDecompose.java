package ru.olegcherednik.zip4jvm.engine.decompose;

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
final class EndCentralDirectoryDecompose extends BaseDecompose {

    public EndCentralDirectoryDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        super(blockModel, settings);
    }

    public EndCentralDirectoryView createView() {
        return EndCentralDirectoryView.builder()
                                      .endCentralDirectory(blockModel.getEndCentralDirectory())
                                      .block(blockModel.getEndCentralDirectoryBlock())
                                      .charset(settings.getCharset())
                                      .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

    public void write(Path destDir) throws IOException {
        try (PrintStream out = new PrintStream(destDir.resolve("end_central_directory.txt").toFile())) {
            createView().print(out);
        }

        copyLarge(blockModel.getZipModel().getFile(), destDir.resolve("end_central_directory.data"), blockModel.getEndCentralDirectoryBlock());
    }

}
