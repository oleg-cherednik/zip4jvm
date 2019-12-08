package ru.olegcherednik.zip4jvm.engine.decompose;

import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.centraldirectory.CentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.centraldirectory.FileHeaderView;

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

    public boolean print(PrintStream out, boolean emptyLine) {
        return createView().print(out, emptyLine);
    }

    public void write(Path destDir) throws IOException {
        Path dir = Files.createDirectories(destDir.resolve(FILE_NAME));

        writeTextInfo(dir);
        writeFileHeaders(dir);
        writeDigitalSignature(dir);
    }

    private void writeTextInfo(Path dir) throws IOException {
        Utils.print(dir.resolve(FILE_NAME + ".txt"), out -> createView().printHeader(out));
    }

    private CentralDirectoryView createView() {
        return CentralDirectoryView.builder()
                                   .centralDirectory(centralDirectory)
                                   .diagCentralDirectory(block)
                                   .getDataFunc(Utils.getDataFunc(zipModel))
                                   .charset(settings.getCharset())
                                   .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

    private void writeFileHeaders(Path dir) throws IOException {
        int pos = 0;

        for (CentralDirectory.FileHeader fileHeader : centralDirectory.getFileHeaders()) {
            String fileName = fileHeader.getFileName();
            CentralDirectoryBlock.FileHeaderBlock fileHeaderBlock = block.getFileHeaderBlock(fileName);
            Path subDir = Utils.createSubDir(dir, zipModel.getZipEntryByFileName(fileName), pos);

            writeFileHeader(subDir, fileHeader, fileHeaderBlock, pos);
            writeExtraField(subDir, fileHeader, fileHeaderBlock.getExtraFieldBlock());

            pos++;
        }
    }

    private void writeFileHeader(Path dir, CentralDirectory.FileHeader fileHeader, CentralDirectoryBlock.FileHeaderBlock block, int pos)
            throws IOException {
        String fileName = "file_header";
        FileHeaderView view = FileHeaderView.builder()
                                            .fileHeader(fileHeader)
                                            .block(block)
                                            .pos(pos)
                                            .charset(settings.getCharset())
                                            .position(settings.getOffs(), settings.getColumnWidth()).build();

        Utils.print(dir.resolve(fileName + ".txt"), view::print);
        Utils.copyLarge(zipModel, dir.resolve(fileName + ".data"), block);
    }

    private void writeExtraField(Path dir, CentralDirectory.FileHeader fileHeader, ExtraFieldBlock block) throws IOException {
        ExtraField extraField = fileHeader.getExtraField();
        GeneralPurposeFlag generalPurposeFlag = fileHeader.getGeneralPurposeFlag();
        new ExtraFieldDecompose(zipModel, settings, extraField, block, generalPurposeFlag).write(dir);
    }

    private void writeDigitalSignature(Path dir) {
        // TODO write DigitalSignature

    }

}
