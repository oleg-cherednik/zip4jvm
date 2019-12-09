package ru.olegcherednik.zip4jvm.engine.decompose;

import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.centraldirectory.CentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.centraldirectory.DigitalSignatureView;

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
        emptyLine |= centralDirectoryView().print(out, emptyLine);
        emptyLine |= fileHeaderDecompose().printTextInfo(out, emptyLine);
        return digitalSignatureView().print(out, emptyLine);
    }

    public void decompose(Path dir) throws IOException {
        dir = Files.createDirectories(dir.resolve(FILE_NAME));

        printTextInfo(dir);
        fileHeaderDecompose().decompose(dir);
        digitalSignature(dir);
    }

    private void printTextInfo(Path dir) throws IOException {
        Utils.print(dir.resolve(FILE_NAME + ".txt"), out -> centralDirectoryView().print(out));
    }

    private void digitalSignature(Path dir) throws FileNotFoundException {
        if (centralDirectory.getDigitalSignature() == null)
            return;

        String fileName = "digital_signature";
        Utils.print(dir.resolve(fileName + ".txt"), out -> digitalSignatureView().print(out));
        // TODO write digital signature data file
    }

    private CentralDirectoryView centralDirectoryView() {
        return new CentralDirectoryView(centralDirectory, block, settings.getOffs(), settings.getColumnWidth());
    }

    private FileHeaderDecompose fileHeaderDecompose() {
        return new FileHeaderDecompose(zipModel, settings, centralDirectory, block);
    }

    private DigitalSignatureView digitalSignatureView() {
        CentralDirectory.DigitalSignature digitalSignature = centralDirectory.getDigitalSignature();
        int offs = settings.getOffs();
        int columnWidth = settings.getColumnWidth();
        return new DigitalSignatureView(digitalSignature, block.getDigitalSignatureBlock(), offs, columnWidth);
    }

}
