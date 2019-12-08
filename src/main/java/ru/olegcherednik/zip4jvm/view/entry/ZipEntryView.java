package ru.olegcherednik.zip4jvm.view.entry;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.ZipEntryBlock;
import ru.olegcherednik.zip4jvm.view.IView;
import ru.olegcherednik.zip4jvm.view.View;
import ru.olegcherednik.zip4jvm.view.crypto.EncryptionHeaderView;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 27.10.2019
 */
public final class ZipEntryView extends View {

    private final long pos;
    private final LocalFileHeader localFileHeader;
    private final ZipEntryBlock.LocalFileHeaderBlock diagLocalFileHeader;
    private final ZipEntryBlock.EncryptionHeader encryptionHeader;
    // TODO duplication of data descriptor
    private final DataDescriptor dataDescriptor;
    private final Block blockDataDescriptor;
    private final Function<Block, byte[]> getDataFunc;
    private final Charset charset;

    public static Builder builder() {
        return new Builder();
    }

    private ZipEntryView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        pos = builder.pos;
        localFileHeader = builder.localFileHeader;
        diagLocalFileHeader = builder.diagLocalFileHeader;
        encryptionHeader = builder.encryptionHeader;
        dataDescriptor = builder.dataDescriptor;
        blockDataDescriptor = builder.blockDataDescriptor;
        getDataFunc = builder.getDataFunc;
        charset = builder.charset;
    }

    @Override
    public boolean print(PrintStream out) {
        boolean emptyLine = createLocalFileHeaderView().print(out);
        emptyLine = createEncryptionHeaderView().print(out, emptyLine);
        return createDataDescriptorView().print(out, emptyLine);
    }

    public LocalFileHeaderView createLocalFileHeaderView() {
        return LocalFileHeaderView.builder()
                                  .localFileHeader(localFileHeader)
                                  .diagLocalFileHeader(diagLocalFileHeader)
                                  .pos(pos)
                                  .getDataFunc(getDataFunc)
                                  .charset(charset)
                                  .offs(offs)
                                  .columnWidth(columnWidth).build();
    }

    public EncryptionHeaderView createEncryptionHeaderView() {
        return EncryptionHeaderView.builder()
                                   .encryptionHeader(encryptionHeader)
                                   .pos(pos)
                                   .offs(offs)
                                   .columnWidth(columnWidth).build();
    }

    private IView createDataDescriptorView() {
        return DataDescriptorView.builder()
                                 .dataDescriptor(dataDescriptor)
                                 .block(blockDataDescriptor)
                                 .pos(pos)
                                 .offs(offs)
                                 .columnWidth(columnWidth).build();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private long pos;
        private LocalFileHeader localFileHeader;
        private ZipEntryBlock.LocalFileHeaderBlock diagLocalFileHeader;
        private ZipEntryBlock.EncryptionHeader encryptionHeader;
        private DataDescriptor dataDescriptor;
        private Block blockDataDescriptor = Block.NULL;
        private Function<Block, byte[]> getDataFunc = block -> ArrayUtils.EMPTY_BYTE_ARRAY;
        private Charset charset = Charsets.IBM437;
        private int offs;
        private int columnWidth;

        public ZipEntryView build() {
            return new ZipEntryView(this);
        }

        public Builder pos(long pos) {
            this.pos = pos;
            return this;
        }

        public Builder localFileHeader(LocalFileHeader localFileHeader) {
            this.localFileHeader = localFileHeader;
            return this;
        }

        public Builder diagLocalFileHeader(ZipEntryBlock.LocalFileHeaderBlock diagLocalFileHeader) {
            this.diagLocalFileHeader = diagLocalFileHeader;
            return this;
        }

        public Builder encryptionHeader(ZipEntryBlock.EncryptionHeader encryptionHeader) {
            this.encryptionHeader = encryptionHeader;
            return this;
        }

        public Builder dataDescriptor(DataDescriptor dataDescriptor) {
            this.dataDescriptor = dataDescriptor;
            return this;
        }

        public Builder blockDataDescriptor(Block blockDataDescriptor) {
            this.blockDataDescriptor = Optional.ofNullable(blockDataDescriptor).orElse(Block.NULL);
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

        public Builder offs(int offs) {
            this.offs = offs;
            return this;
        }

        public Builder columnWidth(int columnWidth) {
            this.columnWidth = columnWidth;
            return this;
        }
    }

}
