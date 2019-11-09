package ru.olegcherednik.zip4jvm.view.crypto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.io.readers.block.aes.BlockAesEncryptionHeader;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.stream.IntStream;

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
    public void print(PrintStream out) {
        String str = String.format("#%d (AES) encryption header", pos + 1);
        out.println(str);

        IntStream.range(0, str.length()).forEach(i -> out.print('-'));

        out.println();
        out.format("%ssalt:                                           %d bytes\n", prefix, encryptionHeader.getSalt().getSize());
        out.format("%s  - location:                                   %2$d (0x%2$08X) bytes\n", prefix, encryptionHeader.getSalt().getOffs());

        ByteArrayHexView.builder()
                        .buf(encryptionHeader.getSalt().getData())
                        .offs(prefix.length())
                        .columnWidth(52).build().print(out);

        out.format("%spassword checksum:                              %d bytes\n",
                prefix, encryptionHeader.getPasswordChecksum().getSize());
        out.format("%s  - location:                                   %2$d (0x%2$08X) bytes\n",
                prefix, encryptionHeader.getPasswordChecksum().getOffs());

        ByteArrayHexView.builder()
                        .buf(encryptionHeader.getPasswordChecksum().getData())
                        .offs(prefix.length())
                        .columnWidth(52).build().print(out);

        out.format("%smac:                                            %d bytes\n", prefix, encryptionHeader.getMac().getSize());
        out.format("%s  - location:                                   %2$d (0x%2$08X) bytes\n", prefix, encryptionHeader.getMac().getOffs());

        ByteArrayHexView.builder()
                        .buf(encryptionHeader.getMac().getData())
                        .offs(prefix.length())
                        .columnWidth(52).build().print(out);
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
