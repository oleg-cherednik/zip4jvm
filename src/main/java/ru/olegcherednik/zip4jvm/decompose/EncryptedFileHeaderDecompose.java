package ru.olegcherednik.zip4jvm.decompose;

import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BaseCentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.centraldirectory.EncryptedFileHeaderView;
import ru.olegcherednik.zip4jvm.view.centraldirectory.FileHeaderView;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 03.01.2023
 */
public class EncryptedFileHeaderDecompose extends FileHeaderDecompose {

    public EncryptedFileHeaderDecompose(ZipModel zipModel,
                                        ZipInfoSettings settings,
                                        CentralDirectory centralDirectory,
                                        BaseCentralDirectoryBlock block) {
        super(zipModel, settings, centralDirectory, block);
    }

    @Override
    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        return super.printTextInfo(out, emptyLine);
    }

    @Override
    public Path decompose(Path dir) throws IOException {
        return super.decompose(dir);
    }

    @Override
    protected FileHeaderView fileHeaderView(CentralDirectory.FileHeader fileHeader, CentralDirectoryBlock.FileHeaderBlock block, long pos) {
        return new EncryptedFileHeaderView(fileHeader,
                                           block,
                                           pos,
                                           settings.getCharset(),
                                           settings.getOffs(),
                                           settings.getColumnWidth(),
                                           zipModel.getTotalDisks());
    }
}
