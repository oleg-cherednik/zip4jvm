package ru.olegcherednik.zip4jvm.engine.decompose;

import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.EndCentralDirectoryView;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
final class EndCentralDirectoryDecompose extends BaseDecompose {

    private static final String FILE_NAME = "end_central_directory";

    private final EndCentralDirectory endCentralDirectory;
    private final Block block;
    private final Path file;

    public EndCentralDirectoryDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        super(blockModel, settings);
        endCentralDirectory = blockModel.getEndCentralDirectory();
        block = blockModel.getEndCentralDirectoryBlock();
        file = blockModel.getZipModel().getFile();
    }

    @Override
    protected EndCentralDirectoryView createView() {
        return EndCentralDirectoryView.builder()
                                      .endCentralDirectory(endCentralDirectory)
                                      .block(block)
                                      .charset(settings.getCharset())
                                      .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

    @Override
    public void write(Path destDir) throws IOException {
        print(destDir.resolve(FILE_NAME + ".txt"));
        copyLarge(file, destDir.resolve(FILE_NAME + ".data"), block);
    }

}
