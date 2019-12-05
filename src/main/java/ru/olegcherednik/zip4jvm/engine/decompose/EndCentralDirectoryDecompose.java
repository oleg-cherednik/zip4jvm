package ru.olegcherednik.zip4jvm.engine.decompose;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
final class EndCentralDirectoryDecompose extends BaseDecompose {

    private final BlockModel blockModel;
    private final ZipInfoSettings settings;

    public EndCentralDirectoryView createEndCentralDirectoryView() {
        return EndCentralDirectoryView.builder()
                                      .endCentralDirectory(blockModel.getEndCentralDirectory())
                                      .block(blockModel.getEndCentralDirectoryBlock())
                                      .charset(settings.getCharset())
                                      .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

    public void writeEndCentralDirectory(Path destDir) throws IOException {
        try (PrintStream out = new PrintStream(destDir.resolve("end_central_directory.txt").toFile())) {
            createEndCentralDirectoryView().print(out);
        }

        copyLarge(blockModel.getZipModel().getFile(), destDir.resolve("end_central_directory.data"), blockModel.getEndCentralDirectoryBlock());
    }


}
