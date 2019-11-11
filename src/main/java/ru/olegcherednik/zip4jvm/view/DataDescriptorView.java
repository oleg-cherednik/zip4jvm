package ru.olegcherednik.zip4jvm.view;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.block.Block;

import java.io.PrintStream;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
public final class DataDescriptorView extends View {

    private final DataDescriptor dataDescriptor;
    private final Block block;
    private final long pos;

    public static Builder builder() {
        return new Builder();
    }

    private DataDescriptorView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        dataDescriptor = builder.dataDescriptor;
        block = builder.block;
        pos = builder.pos;
    }

    @Override
    public boolean print(PrintStream out) {
        if (dataDescriptor == null || block == Block.NULL)
            return false;

        printTitle(out, String.format("#%d (%s) Data descriptor", pos + 1, ViewUtils.signature(DataDescriptor.SIGNATURE)));
        out.println();
        printLine(out, "- location:", String.format("%1$d (0x%1$08X) bytes", block.getOffs()));
        printLine(out, "- size:", String.format("%d bytes", block.getSize()));
        printLine(out, "32-bit CRC value:", String.format("0x%08X", dataDescriptor.getCrc32()));
        printLine(out, "compressed size:", String.format("%d bytes", dataDescriptor.getCompressedSize()));
        printLine(out, "uncompressed size:", String.format("%d bytes", dataDescriptor.getUncompressedSize()));

        return true;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private DataDescriptor dataDescriptor;
        private Block block = Block.NULL;
        private long pos;
        private int offs;
        private int columnWidth;

        public DataDescriptorView build() {
            return new DataDescriptorView(this);
        }

        public Builder dataDescriptor(DataDescriptor dataDescriptor) {
            this.dataDescriptor = dataDescriptor;
            return this;
        }

        public Builder block(Block block) {
            this.block = Optional.ofNullable(block).orElse(Block.NULL);
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
