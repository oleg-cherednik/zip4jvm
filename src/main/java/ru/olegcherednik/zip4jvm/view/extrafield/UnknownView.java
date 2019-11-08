package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class UnknownView extends View {

    private final ExtraField.Record.Unknown record;
    private final Block block;

    public static Builder builder() {
        return new Builder();
    }

    private UnknownView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        record = builder.record;
        block = builder.block;
    }

    @Override
    public void print(PrintStream out) {
        printLine(out, String.format("(0x%04X) Unknown:", record.getSignature()), String.format("%d bytes", block.getSize()));
        printLine(out, "  - location:", String.format("%1$d (0x%1$08X) bytes", block.getSize()));

        ByteArrayHexView.builder()
                        .buf(record.getBlockData())
                        .offs(offs)
                        .columnWidth(columnWidth).build().print(out);
    }

    public static final class Builder {

        private ExtraField.Record.Unknown record;
        private Block block;
        private int offs;
        private int columnWidth;

        public UnknownView build() {
            return new UnknownView(this);
        }

        public Builder record(ExtraField.Record.Unknown record) {
            this.record = record;
            return this;
        }

        public Builder block(Block block) {
            this.block = block;
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
