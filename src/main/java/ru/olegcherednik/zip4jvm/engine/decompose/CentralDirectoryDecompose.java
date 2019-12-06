package ru.olegcherednik.zip4jvm.engine.decompose;

import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.centraldirectory.CentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.centraldirectory.FileHeaderView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
final class CentralDirectoryDecompose extends BaseDecompose {

    private static final String FILE_NAME = "central_directory";

    private final CentralDirectory centralDirectory;
    private final CentralDirectoryBlock blockA;
    private final ZipModel zipModel;

    public CentralDirectoryDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        super(blockModel, settings);
        centralDirectory = blockModel.getCentralDirectory();
        blockA = blockModel.getCentralDirectoryBlock();
        zipModel = blockModel.getZipModel();
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
        Path dir = destDir.resolve(FILE_NAME);
        Files.createDirectories(dir);

        try (PrintStream out = new PrintStream(new FileOutputStream(dir.resolve(FILE_NAME + ".txt").toFile(), true))) {
            createView().printHeader(out);
        }

        int pos = 0;

        for (CentralDirectory.FileHeader fileHeader : centralDirectory.getFileHeaders()) {
            String fileName = fileHeader.getFileName();
            ZipEntry zipEntry = blockModel.getZipModel().getZipEntryByFileName(fileName);
            CentralDirectoryBlock.FileHeaderBlock block = blockA.getFileHeaderBlock(fileName);

            if (zipEntry.isDirectory())
                fileName = fileName.substring(0, fileName.length() - 1);

            fileName = "#" + (pos + 1) + " - " + fileName.replaceAll("[\\/]", "_-_");

            Path subDir = dir.resolve(fileName);
            Files.createDirectories(subDir);

            try (PrintStream out = new PrintStream(new FileOutputStream(subDir.resolve("file_header.txt").toFile()))) {
                FileHeaderView.builder()
                              .fileHeader(fileHeader)
                              .diagFileHeader(block)
                              .getDataFunc(getDataFunc(zipModel))
                              .pos(pos)
                              .charset(settings.getCharset())
                              .offs(settings.getOffs())
                              .columnWidth(settings.getColumnWidth()).build().print(out);
            }

            copyLarge(blockModel.getZipModel().getFile(), subDir.resolve("file_header.data"), block);
            writeExtraField(fileHeader.getExtraField(), block.getExtraFieldBlock(), fileHeader.getGeneralPurposeFlag(), subDir);

            pos++;
        }
    }

}
