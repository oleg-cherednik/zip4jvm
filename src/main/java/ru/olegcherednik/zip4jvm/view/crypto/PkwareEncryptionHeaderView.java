package ru.olegcherednik.zip4jvm.view.crypto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.io.readers.block.pkware.PkwareEncryptionHeader;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
final class PkwareEncryptionHeaderView extends View {

    private final PkwareEncryptionHeader encryptionHeader;
    private final long pos;

    public static Builder builder() {
        return new Builder();
    }

    private PkwareEncryptionHeaderView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        encryptionHeader = builder.encryptionHeader;
        pos = builder.pos;
    }

    @Override
    public boolean print(PrintStream out) {
        printTitle(out, String.format("#%d (PKWARE) encryption header", pos + 1));
        out.println();
        printLine(out, "data:", String.format("%d bytes", encryptionHeader.getData().getSize()));
        printLine(out, "- location:", String.format("%1$d (0x%1$08X) bytes", encryptionHeader.getData().getOffs()));

        ByteArrayHexView.builder()
                        .buf(encryptionHeader.getData().getData())
                        .offs(offs)
                        .columnWidth(columnWidth).build().print(out);

        return true;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private PkwareEncryptionHeader encryptionHeader;
        private long pos;
        private int offs;
        private int columnWidth;

        public PkwareEncryptionHeaderView build() {
            return new PkwareEncryptionHeaderView(this);
        }

        public Builder encryptionHeader(PkwareEncryptionHeader encryptionHeader) {
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
