package ru.olegcherednik.zip4jvm.engine.decompose;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.centraldirectory.CentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.centraldirectory.FileHeaderView;
import ru.olegcherednik.zip4jvm.view.extrafield.ExtraFieldView;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 08.12.2019
 */
@RequiredArgsConstructor
final class FileHeaderListDecompose {

    private final ZipModel zipModel;
    private final ZipInfoSettings settings;
    private final CentralDirectory centralDirectory;
    private final CentralDirectoryBlock block;
    private final CentralDirectoryView view;

    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        int pos = 0;

        for (CentralDirectory.FileHeader fileHeader : centralDirectory.getFileHeaders()) {
            if (pos != 0 || emptyLine)
                out.println();

            CentralDirectoryBlock.FileHeaderBlock fileHeaderBlock = block.getFileHeaderBlock(fileHeader.getFileName());
            fileHeaderView(fileHeader, fileHeaderBlock, pos).print(out);
            extraFieldView(fileHeader, fileHeaderBlock).print(out);

            pos++;
        }

        return !centralDirectory.getFileHeaders().isEmpty();
    }

    private FileHeaderView fileHeaderView(CentralDirectory.FileHeader fileHeader, CentralDirectoryBlock.FileHeaderBlock block, int pos) {
        return FileHeaderView.builder()
                             .fileHeader(fileHeader)
                             .block(block)
                             .pos(pos++)
                             .charset(settings.getCharset())
                             .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

    private ExtraFieldView extraFieldView(CentralDirectory.FileHeader fileHeader, CentralDirectoryBlock.FileHeaderBlock block) {
        return ExtraFieldView.builder()
                             .extraField(fileHeader.getExtraField())
                             .block(block.getExtraFieldBlock())
                             .generalPurposeFlag(fileHeader.getGeneralPurposeFlag())
                             .getDataFunc(Utils.getDataFunc(zipModel))
                             .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

    public void write(Path dir) throws IOException {
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
}
