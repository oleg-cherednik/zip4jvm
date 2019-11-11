package ru.olegcherednik.zip4jvm.view.crypto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.io.readers.block.aes.BlockAesEncryptionHeader;
import ru.olegcherednik.zip4jvm.io.readers.block.pkware.PkwareEncryptionHeader;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 24.10.2019
 */
public final class EncryptionHeaderView extends View {

    private final Diagnostic.ZipEntryBlock.EncryptionHeader encryptionHeader;
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

        if (encryptionHeader instanceof BlockAesEncryptionHeader)
            createView((BlockAesEncryptionHeader)encryptionHeader).print(out);
        else if (encryptionHeader instanceof PkwareEncryptionHeader)
            createView((PkwareEncryptionHeader)encryptionHeader).print(out);
        // TODO add for unknown encryption header

        return true;
    }

    private BlockAesEncryptionHeaderView createView(BlockAesEncryptionHeader encryptionHeader) {
        return BlockAesEncryptionHeaderView.builder()
                                           .encryptionHeader(encryptionHeader)
                                           .pos(pos)
                                           .offs(offs)
                                           .columnWidth(columnWidth).build();
    }

    private PkwareEncryptionHeaderView createView(PkwareEncryptionHeader encryptionHeader) {
        return PkwareEncryptionHeaderView.builder()
                                         .encryptionHeader(encryptionHeader)
                                         .pos(pos)
                                         .offs(offs)
                                         .columnWidth(columnWidth).build();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private Diagnostic.ZipEntryBlock.EncryptionHeader encryptionHeader;
        private long pos;
        private int offs;
        private int columnWidth;

        public EncryptionHeaderView build() {
            return new EncryptionHeaderView(this);
        }

        public Builder encryptionHeader(Diagnostic.ZipEntryBlock.EncryptionHeader encryptionHeader) {
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
