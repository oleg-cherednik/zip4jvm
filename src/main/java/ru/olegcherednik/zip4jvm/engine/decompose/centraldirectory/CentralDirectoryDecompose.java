package ru.olegcherednik.zip4jvm.engine.decompose.centraldirectory;

import ru.olegcherednik.zip4jvm.engine.decompose.Utils;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.centraldirectory.CentralDirectoryView;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
public final class CentralDirectoryDecompose {

    private static final String FILE_NAME = "central_directory";

    private final ZipModel zipModel;
    private final CentralDirectory centralDirectory;
    private final CentralDirectoryBlock blockA;
    private final ZipInfoSettings settings;

    public CentralDirectoryDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        zipModel = blockModel.getZipModel();
        centralDirectory = blockModel.getCentralDirectory();
        blockA = blockModel.getCentralDirectoryBlock();
        this.settings = settings;
    }

    public boolean print(PrintStream out, boolean emptyLine) {
        return createView().print(out, emptyLine);
    }

    public void write(Path destDir) throws IOException {
        Path dir = Files.createDirectories(destDir.resolve(FILE_NAME));
        printHeader(dir);
        printFileHeader(dir);
    }

    private CentralDirectoryView createView() {
        return CentralDirectoryView.builder()
                                   .centralDirectory(centralDirectory)
                                   .diagCentralDirectory(blockA)
                                   .getDataFunc(Utils.getDataFunc(zipModel))
                                   .charset(settings.getCharset())
                                   .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

    private void printHeader(Path dir) throws IOException {
        Utils.print(dir.resolve(FILE_NAME + ".txt"), out -> createView().printHeader(out));
    }

    private void printFileHeader(Path dir) throws IOException {
        int pos = 0;

        for (CentralDirectory.FileHeader fileHeader : centralDirectory.getFileHeaders()) {
            String fileName = fileHeader.getFileName();
            ZipEntry zipEntry = zipModel.getZipEntryByFileName(fileName);
            CentralDirectoryBlock.FileHeaderBlock block = blockA.getFileHeaderBlock(fileName);

            if (zipEntry.isDirectory())
                fileName = fileName.substring(0, fileName.length() - 1);

            fileName = "#" + (pos + 1) + " - " + fileName.replaceAll("[\\/]", "_-_");
            Path subDir = dir.resolve(fileName);
            Files.createDirectories(subDir);

            new FileHeaderDecompose(zipModel, settings, fileHeader, block, pos).write(subDir);

            pos++;
        }
    }

}
