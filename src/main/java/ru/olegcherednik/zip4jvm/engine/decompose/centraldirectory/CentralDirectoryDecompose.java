package ru.olegcherednik.zip4jvm.engine.decompose.centraldirectory;

import ru.olegcherednik.zip4jvm.engine.decompose.BaseDecompose;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.centraldirectory.CentralDirectoryView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
public final class CentralDirectoryDecompose extends BaseDecompose {

    private static final String FILE_NAME = "central_directory";

    private final CentralDirectory centralDirectory;
    private final CentralDirectoryBlock blockA;

    public CentralDirectoryDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        super(blockModel.getZipModel(), settings);
        centralDirectory = blockModel.getCentralDirectory();
        blockA = blockModel.getCentralDirectoryBlock();
    }

    @Override
    protected CentralDirectoryView createView() {
        return CentralDirectoryView.builder()
                                   .centralDirectory(centralDirectory)
                                   .diagCentralDirectory(blockA)
                                   .getDataFunc(getDataFunc(zipModel))
                                   .charset(settings.getCharset())
                                   .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

    @Override
    public void write(Path destDir) throws IOException {
        Path dir = Files.createDirectories(destDir.resolve(FILE_NAME));
        printHeader(dir);
        printFileHeader(dir);
    }

    private void printHeader(Path dir) throws IOException {
        print(dir.resolve(FILE_NAME + ".txt"), out -> createView().printHeader(out));
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

            writeExtraField(fileHeader.getExtraField(), block.getExtraFieldBlock(), fileHeader.getGeneralPurposeFlag(), subDir);

            pos++;
        }
    }

}
