package ru.olegcherednik.zip4jvm.view.centraldirectory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.view.View;
import ru.olegcherednik.zip4jvm.view.extrafield.ExtraFieldView;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 11.11.2019
 */
public final class FileHeaderListView extends View {

    private final CentralDirectory centralDirectory;
    private final CentralDirectoryBlock diagCentralDirectory;
    private final Function<Block, byte[]> getDataFunc;
    private final Charset charset;

    public static Builder builder() {
        return new Builder();
    }

    private FileHeaderListView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        centralDirectory = builder.centralDirectory;
        diagCentralDirectory = builder.diagCentralDirectory;
        getDataFunc = builder.getDataFunc;
        charset = builder.charset;
    }

    @Override
    public boolean print(PrintStream out) {
        if (centralDirectory.getFileHeaders().isEmpty())
            return false;

        out.println();

        int pos = 0;

        for (CentralDirectory.FileHeader fileHeader : centralDirectory.getFileHeaders()) {
            if (pos != 0)
                out.println();

            CentralDirectoryBlock.FileHeaderBlock fileHeaderBlock = diagCentralDirectory.getFileHeaderBlock(fileHeader.getFileName());

            FileHeaderView.builder()
                          .fileHeader(fileHeader)
                          .block(fileHeaderBlock)
                          .pos(pos++)
                          .charset(charset)
                          .position(offs, columnWidth).build().print(out);
            ExtraFieldView.builder()
                          .extraField(fileHeader.getExtraField())
                          .block(fileHeaderBlock.getExtraFieldBlock())
                          .generalPurposeFlag(fileHeader.getGeneralPurposeFlag())
                          .getDataFunc(getDataFunc)
                          .position(offs, columnWidth).build().print(out);
        }

        return true;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private CentralDirectory centralDirectory;
        private CentralDirectoryBlock diagCentralDirectory;
        private Function<Block, byte[]> getDataFunc = block -> ArrayUtils.EMPTY_BYTE_ARRAY;
        private Charset charset = Charsets.IBM437;
        private int offs;
        private int columnWidth;

        public FileHeaderListView build() {
            return new FileHeaderListView(this);
        }

        public Builder centralDirectory(CentralDirectory centralDirectory) {
            this.centralDirectory = centralDirectory;
            return this;
        }

        public Builder diagCentralDirectory(CentralDirectoryBlock diagCentralDirectory) {
            this.diagCentralDirectory = diagCentralDirectory;
            return this;
        }

        public Builder getDataFunc(Function<Block, byte[]> getDataFunc) {
            this.getDataFunc = Optional.ofNullable(getDataFunc).orElseGet(() -> block -> ArrayUtils.EMPTY_BYTE_ARRAY);
            return this;
        }

        public Builder charset(Charset charset) {
            this.charset = Optional.ofNullable(charset).orElse(Charsets.IBM437);
            return this;
        }

        public Builder position(int offs, int columnWidth) {
            this.offs = offs;
            this.columnWidth = columnWidth;
            return this;
        }
    }
}
