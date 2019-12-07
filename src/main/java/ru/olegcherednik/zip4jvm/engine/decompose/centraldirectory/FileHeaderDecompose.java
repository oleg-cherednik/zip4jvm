package ru.olegcherednik.zip4jvm.engine.decompose.centraldirectory;

import ru.olegcherednik.zip4jvm.engine.decompose.BaseDecompose;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.IView;
import ru.olegcherednik.zip4jvm.view.centraldirectory.FileHeaderView;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 07.12.2019
 */
final class FileHeaderDecompose extends BaseDecompose {

    private static final String FILE_NAME = "file_header";

    private final CentralDirectory.FileHeader fileHeader;
    private final CentralDirectoryBlock.FileHeaderBlock block;
    private final int pos;

    public FileHeaderDecompose(ZipModel zipModel, ZipInfoSettings settings, CentralDirectory.FileHeader fileHeader,
            CentralDirectoryBlock.FileHeaderBlock block, int pos) {
        super(zipModel, settings);
        this.fileHeader = fileHeader;
        this.block = block;
        this.pos = pos;
    }

    @Override
    protected IView createView() {
        return FileHeaderView.builder()
                             .fileHeader(fileHeader)
                             .diagFileHeader(block)
                             .getDataFunc(getDataFunc(zipModel))
                             .pos(pos)
                             .charset(settings.getCharset())
                             .offs(settings.getOffs())
                             .columnWidth(settings.getColumnWidth()).build();
    }

    @Override
    public void write(Path destDir) throws IOException {
        print(destDir.resolve(FILE_NAME + ".txt"), out -> createView().print(out));
        copyLarge(zipModel.getFile(), destDir.resolve(FILE_NAME + ".data"), block);
    }

}
