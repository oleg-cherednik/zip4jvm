package ru.olegcherednik.zip4jvm.engine.decompose;

import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.EndCentralDirectoryView;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
final class EndCentralDirectoryDecompose {

    private final ZipModel zipModel;
    private final ZipInfoSettings settings;
    private final EndCentralDirectory endCentralDirectory;
    private final Block block;

    public EndCentralDirectoryDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        zipModel = blockModel.getZipModel();
        this.settings = settings;
        endCentralDirectory = blockModel.getEndCentralDirectory();
        block = blockModel.getEndCentralDirectoryBlock();
    }

    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        return createView().print(out, emptyLine);
    }

    public void decompose(Path dir) throws IOException {
        DecomposeUtils.print(dir.resolve("end_central_directory.txt"), out -> createView().print(out));
        DecomposeUtils.copyLarge(zipModel, dir.resolve("end_central_directory.data"), block);
    }

    private EndCentralDirectoryView createView() {
        Charset charset = settings.getCharset();
        int offs = settings.getOffs();
        int columnWidth = settings.getColumnWidth();
        return new EndCentralDirectoryView(endCentralDirectory, block, charset, offs, columnWidth);
    }

}
