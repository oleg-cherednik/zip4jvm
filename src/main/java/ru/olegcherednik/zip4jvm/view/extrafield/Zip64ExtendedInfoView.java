package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class Zip64ExtendedInfoView extends View {

    private final Zip64.ExtendedInfo record;
    private final Block block;

    public static Builder builder() {
        return new Builder();
    }

    private Zip64ExtendedInfoView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        record = builder.record;
        block = builder.block;
    }

    @Override
    public void print(PrintStream out) {
        printLine(out, String.format("(0x%04X) Zip64 Extended Information:", record.getSignature()), String.format("%d bytes", block.getSize()));
        printLine(out, "  - location:", String.format("%1$d (0x%1$08X) bytes", block.getSize()));

        if (record.getUncompressedSize() != ExtraField.NO_DATA)
            printLine(out, "  original compressed size:", String.format("%d bytes", record.getUncompressedSize()));
        if (record.getCompressedSize() != ExtraField.NO_DATA)
            printLine(out, "  original uncompressed size:", String.format("%d bytes", record.getCompressedSize()));
        if (record.getLocalFileHeaderOffs() != ExtraField.NO_DATA)
            printLine(out, "  original relative offset of local header:", String.format("%1$d (0x%1$08X) bytes", record.getLocalFileHeaderOffs()));
        if (record.getDisk() != ExtraField.NO_DATA)
            printLine(out, String.format("  original part number of this part (%04X):", record.getDisk()), String.format("%d", record.getDisk()));
    }

    public static final class Builder {

        private Zip64.ExtendedInfo record;
        private Block block;
        private int offs;
        private int columnWidth;

        public Zip64ExtendedInfoView build() {
            return new Zip64ExtendedInfoView(this);
        }

        public Builder record(Zip64.ExtendedInfo record) {
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
