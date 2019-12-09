package ru.olegcherednik.zip4jvm.engine.decompose;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.centraldirectory.FileHeaderView;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 08.12.2019
 */
@RequiredArgsConstructor
final class FileHeaderDecompose {

    private final ZipModel zipModel;
    private final ZipInfoSettings settings;
    private final CentralDirectory centralDirectory;
    private final CentralDirectoryBlock block;

    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        long pos = 0;

        for (CentralDirectory.FileHeader fileHeader : centralDirectory.getFileHeaders()) {
            CentralDirectoryBlock.FileHeaderBlock fileHeaderBlock = block.getFileHeaderBlock(fileHeader.getFileName());

            emptyLine |= fileHeaderView(fileHeader, fileHeaderBlock, pos).print(out, pos != 0 || emptyLine);
            emptyLine |= extraFieldDecompose(fileHeader, fileHeaderBlock.getExtraFieldBlock()).printTextInfo(out, false);

            pos++;
        }

        return emptyLine;
    }

    public void decompose(Path dir) throws IOException {
        long pos = 0;

        for (CentralDirectory.FileHeader fileHeader : centralDirectory.getFileHeaders()) {
            String fileName = fileHeader.getFileName();
            CentralDirectoryBlock.FileHeaderBlock fileHeaderBlock = block.getFileHeaderBlock(fileName);
            Path subDir = Utils.createSubDir(dir, zipModel.getZipEntryByFileName(fileName), pos);

            fileHeader(subDir, fileHeader, fileHeaderBlock, pos);
            extraFieldDecompose(fileHeader, fileHeaderBlock.getExtraFieldBlock()).decompose(subDir);

            pos++;
        }
    }

    private void fileHeader(Path dir, CentralDirectory.FileHeader fileHeader, CentralDirectoryBlock.FileHeaderBlock block, long pos)
            throws IOException {
        String fileName = "file_header";

        Utils.print(dir.resolve(fileName + ".txt"), out -> fileHeaderView(fileHeader, block, pos).print(out));
        Utils.copyLarge(zipModel, dir.resolve(fileName + ".data"), block);
    }

    private FileHeaderView fileHeaderView(CentralDirectory.FileHeader fileHeader, CentralDirectoryBlock.FileHeaderBlock block, long pos) {
        return FileHeaderView.builder()
                             .fileHeader(fileHeader)
                             .block(block)
                             .pos(pos)
                             .charset(settings.getCharset())
                             .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

    private ExtraFieldDecompose extraFieldDecompose(CentralDirectory.FileHeader fileHeader, ExtraFieldBlock block) {
        ExtraField extraField = fileHeader.getExtraField();
        GeneralPurposeFlag generalPurposeFlag = fileHeader.getGeneralPurposeFlag();
        return new ExtraFieldDecompose(zipModel, settings, extraField, block, generalPurposeFlag);
    }

}
