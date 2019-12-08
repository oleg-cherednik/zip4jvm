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

    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        if (zip64 == Zip64.NULL)
            return false;

        emptyLine = createEndCentralDirectorLocatorView().print(out, emptyLine);
        return createEndCentralDirectoryView().print(out, emptyLine);
    }

    public void write(Path dir) throws IOException {
        if (zip64 == Zip64.NULL)
            return;

        dir = Files.createDirectories(dir.resolve("zip64"));

        writeEndOfCentralDirectoryLocator(dir);
        writeEndOfCentralDirectory(dir);
    }

    private void writeEndOfCentralDirectoryLocator(Path dir) throws IOException {
        String fileName = "zip64_end_central_directory_locator";

        Utils.print(dir.resolve(fileName + ".txt"), out -> createEndCentralDirectorLocatorView().print(out));
        Utils.copyLarge(zipModel, dir.resolve(fileName + ".data"), block.getEndCentralDirectoryLocatorBlock());
    }

    private void writeEndOfCentralDirectory(Path dir) throws IOException {
        String fileName = "zip64_end_central_directory";

        Utils.print(dir.resolve(fileName + ".txt"), out -> createEndCentralDirectoryView().print(out));
        Utils.copyLarge(zipModel, dir.resolve(fileName + ".data"), block.getEndCentralDirectoryBlock());
    }

    private Zip64View.EndCentralDirectoryLocatorView createEndCentralDirectorLocatorView() {
        Zip64.EndCentralDirectoryLocator locator = zip64.getEndCentralDirectoryLocator();
        int offs = settings.getOffs();
        int columnWidth = settings.getColumnWidth();
        return new Zip64View.EndCentralDirectoryLocatorView(locator, block.getEndCentralDirectoryLocatorBlock(), offs, columnWidth);
    }

    private Zip64View.EndCentralDirectoryView createEndCentralDirectoryView() {
        Zip64.EndCentralDirectory dir = zip64.getEndCentralDirectory();
        int offs = settings.getOffs();
        int columnWidth = settings.getColumnWidth();
        return new Zip64View.EndCentralDirectoryView(dir, block.getEndCentralDirectoryBlock(), offs, columnWidth);
    }

}
