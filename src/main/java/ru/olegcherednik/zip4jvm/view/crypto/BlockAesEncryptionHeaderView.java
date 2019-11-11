package ru.olegcherednik.zip4jvm.view.crypto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.io.readers.block.aes.BlockAesEncryptionHeader;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
final class BlockAesEncryptionHeaderView extends View {

    private final BlockAesEncryptionHeader encryptionHeader;
    private final long pos;

    public static Builder builder() {
        return new Builder();
    }

    private BlockAesEncryptionHeaderView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        encryptionHeader = builder.encryptionHeader;
        pos = builder.pos;
    }

    @Override
    public boolean print(PrintStream out) {
        printSubTitle(out, pos, "(AES) encryption header");
        printSalt(out);
        printPasswordChecksum(out);
        printMac(out);
        return true;
    }

    private void printSalt(PrintStream out) {
        printValueLocation(out, "salt:", encryptionHeader.getSalt());

        ByteArrayHexView.builder()
                        .buf(encryptionHeader.getSalt().getData())
                        .offs(offs)
                        .columnWidth(columnWidth).build().print(out);
    }

    private void printPasswordChecksum(PrintStream out) {
        printValueLocation(out, "password checksum:", encryptionHeader.getPasswordChecksum());

        ByteArrayHexView.builder()
                        .buf(encryptionHeader.getPasswordChecksum().getData())
                        .offs(offs)
                        .columnWidth(columnWidth).build().print(out);
    }

    private void printMac(PrintStream out) {
        printValueLocation(out, "mac:", encryptionHeader.getMac());

        ByteArrayHexView.builder()
                        .buf(encryptionHeader.getMac().getData())
                        .offs(offs)
                        .columnWidth(columnWidth).build().print(out);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private BlockAesEncryptionHeader encryptionHeader;
        private long pos;
        private int offs;
        private int columnWidth;

        public BlockAesEncryptionHeaderView build() {
            return new BlockAesEncryptionHeaderView(this);
        }

        public Builder encryptionHeader(BlockAesEncryptionHeader encryptionHeader) {
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
