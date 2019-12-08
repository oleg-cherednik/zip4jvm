package ru.olegcherednik.zip4jvm.engine.decompose;

import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.Zip64Block;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.Zip64View;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
final class Zip64Decompose {

    private final ZipModel zipModel;
    private final ZipInfoSettings settings;
    private final Zip64 zip64;
    private final Zip64Block block;

    public Zip64Decompose(BlockModel blockModel, ZipInfoSettings settings) {
        zipModel = blockModel.getZipModel();
        this.settings = settings;
        zip64 = blockModel.getZip64();
        block = blockModel.getZip64Block();
    }

    public boolean print(PrintStream out, boolean emptyLine) {
        return zip64 != Zip64.NULL && createView().print(out, emptyLine);
    }

    public void write(Path dir) throws IOException {
        if (zip64 == Zip64.NULL)
            return;

        dir = Files.createDirectories(dir.resolve("zip64"));
        Zip64View view = createView();

        writeEndOfCentralDirectoryLocator(dir, view.createEndCentralDirectorLocatorView());
        writeEndOfCentralDirectory(dir, view.createEndCentralDirectoryView());
    }

    private Zip64View createView() {
        return Zip64View.builder()
                        .zip64(zip64)
                        .block(block)
                        .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

    private void writeEndOfCentralDirectoryLocator(Path dir, Zip64View.EndCentralDirectoryLocatorView view) throws IOException {
        String fileName = "zip64_end_central_directory_locator";

        Utils.print(dir.resolve(fileName + ".txt"), view::print);
        Utils.copyLarge(zipModel, dir.resolve(fileName + ".data"), block.getEndCentralDirectoryLocatorBlock());
    }

    private void writeEndOfCentralDirectory(Path dir, Zip64View.EndCentralDirectoryView view) throws IOException {
        String fileName = "zip64_end_central_directory";

        Utils.print(dir.resolve(fileName + ".txt"), view::print);
        Utils.copyLarge(zipModel, dir.resolve(fileName + ".data"), block.getEndCentralDirectoryBlock());
    }

}
