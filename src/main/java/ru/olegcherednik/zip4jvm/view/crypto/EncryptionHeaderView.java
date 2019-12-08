package ru.olegcherednik.zip4jvm.view.crypto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.io.readers.block.aes.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.io.readers.block.pkware.PkwareEncryptionHeader;
import ru.olegcherednik.zip4jvm.model.block.ZipEntryBlock;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 24.10.2019
 */
public final class EncryptionHeaderView extends View {

    private final ZipEntryBlock.EncryptionHeader encryptionHeader;
    private final long pos;

    public static Builder builder() {
        return new Builder();
    }

    private EncryptionHeaderView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        encryptionHeader = builder.encryptionHeader;
        pos = builder.pos;
    }

    @Override
    public boolean print(PrintStream out) {
        if (encryptionHeader == null)
            return false;

        if (encryptionHeader instanceof AesEncryptionHeaderBlock)
            createView((AesEncryptionHeaderBlock)encryptionHeader).print(out);
        else if (encryptionHeader instanceof PkwareEncryptionHeader)
            createView((PkwareEncryptionHeader)encryptionHeader).print(out);
        // TODO add for unknown encryption header

        return true;
    }

    public BlockAesEncryptionHeaderView createView(AesEncryptionHeaderBlock encryptionHeader) {
        return BlockAesEncryptionHeaderView.builder()
                                           .encryptionHeader(encryptionHeader)
                                           .pos(pos)
                                           .offs(offs)
                                           .columnWidth(columnWidth).build();
    }

    public PkwareEncryptionHeaderView createView(PkwareEncryptionHeader encryptionHeader) {
        return PkwareEncryptionHeaderView.builder()
                                         .encryptionHeader(encryptionHeader)
                                         .pos(pos)
                                         .offs(offs)
                                         .columnWidth(columnWidth).build();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private ZipEntryBlock.EncryptionHeader encryptionHeader;
        private long pos;
        private int offs;
        private int columnWidth;

        public EncryptionHeaderView build() {
            Objects.requireNonNull(encryptionHeader, "'encryptionHeader' must not be null");
            return new EncryptionHeaderView(this);
        }

        public Builder encryptionHeader(ZipEntryBlock.EncryptionHeader encryptionHeader) {
            this.encryptionHeader = encryptionHeader;
            return this;
        }

        public Builder pos(long pos) {
            this.pos = pos;
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
