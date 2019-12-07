package ru.olegcherednik.zip4jvm.engine.decompose.centraldirectory;

import ru.olegcherednik.zip4jvm.engine.decompose.ExtraFieldDecompose;
import ru.olegcherednik.zip4jvm.engine.decompose.Utils;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.centraldirectory.FileHeaderView;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 07.12.2019
 */
final class FileHeaderDecompose {

    private static final String FILE_NAME = "file_header";

    private final ZipModel zipModel;
    private final ZipInfoSettings settings;
    private final CentralDirectory.FileHeader fileHeader;
    private final CentralDirectoryBlock.FileHeaderBlock block;
    private final int pos;

    public FileHeaderDecompose(ZipModel zipModel, ZipInfoSettings settings, CentralDirectory.FileHeader fileHeader,
            CentralDirectoryBlock.FileHeaderBlock block, int pos) {
        this.zipModel = zipModel;
        this.settings = settings;
        this.fileHeader = fileHeader;
        this.block = block;
        this.pos = pos;
    }

    public void write(Path destDir) throws IOException {
        Utils.print(destDir.resolve(FILE_NAME + ".txt"), out -> createView().print(out));
        Utils.copyLarge(zipModel, destDir.resolve(FILE_NAME + ".data"), block);

        new ExtraFieldDecompose(zipModel, settings, fileHeader.getExtraField(), block.getExtraFieldBlock(), fileHeader.getGeneralPurposeFlag())
                .write(destDir);
    }

    private FileHeaderView createView() {
        return FileHeaderView.builder()
                             .fileHeader(fileHeader)
                             .diagFileHeader(block)
                             .getDataFunc(Utils.getDataFunc(zipModel))
                             .pos(pos)
                             .charset(settings.getCharset())
                             .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

}
