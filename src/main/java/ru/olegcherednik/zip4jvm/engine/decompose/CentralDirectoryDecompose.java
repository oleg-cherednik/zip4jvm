package ru.olegcherednik.zip4jvm.engine.decompose;

import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.centraldirectory.CentralDirectoryView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
final class CentralDirectoryDecompose {

    private static final String FILE_NAME = "central_directory";

    private final ZipModel zipModel;
    private final ZipInfoSettings settings;
    private final CentralDirectory centralDirectory;
    private final CentralDirectoryBlock block;

    public CentralDirectoryDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        zipModel = blockModel.getZipModel();
        this.settings = settings;
        centralDirectory = blockModel.getCentralDirectory();
        block = blockModel.getCentralDirectoryBlock();
    }

    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        CentralDirectoryView view = createView();

        view.printHeader(out, emptyLine);
        emptyLine = new FileHeaderListDecompose(zipModel, settings, centralDirectory, block, view).printTextInfo(out, emptyLine);
        return view.createDigitalSignatureView().print(out, emptyLine);
    }

    public void write(Path destDir) throws IOException {
        Path dir = Files.createDirectories(destDir.resolve(FILE_NAME));
        CentralDirectoryView view = createView();

        printTextInfo(dir);
        new FileHeaderListDecompose(zipModel, settings, centralDirectory, block, view).write(dir);
        writeDigitalSignature(dir, view);
    }

    private void printTextInfo(Path dir) throws IOException {
        Utils.print(dir.resolve(FILE_NAME + ".txt"), out -> createView().printHeader(out, false));
    }

    private CentralDirectoryView createView() {
        return CentralDirectoryView.builder()
                                   .centralDirectory(centralDirectory)
                                   .diagCentralDirectory(block)
                                   .getDataFunc(Utils.getDataFunc(zipModel))
                                   .charset(settings.getCharset())
                                   .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

    private void writeDigitalSignature(Path dir, CentralDirectoryView view) throws FileNotFoundException {
        if (centralDirectory.getDigitalSignature() == null)
            return;

        String fileName = "digital_signature";
        Utils.print(dir.resolve(fileName + ".txt"), out -> view.createDigitalSignatureView().print(out));
    }

}
