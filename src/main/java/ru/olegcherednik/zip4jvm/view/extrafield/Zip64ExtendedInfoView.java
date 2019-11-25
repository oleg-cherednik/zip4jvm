package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class Zip64ExtendedInfoView extends ExtraFieldRecordView {

    private final Zip64.ExtendedInfo record;

    public static Builder builder() {
        return new Builder();
    }

    private Zip64ExtendedInfoView(Builder builder) {
        super(builder.block, builder.offs, builder.columnWidth);
        record = builder.record;
    }

    @Override
    public boolean print(PrintStream out) {
        super.print(out);

        if (record.getUncompressedSize() != ExtraField.NO_DATA)
            printLine(out, "  original compressed size:", String.format("%d bytes", record.getUncompressedSize()));
        if (record.getCompressedSize() != ExtraField.NO_DATA)
            printLine(out, "  original uncompressed size:", String.format("%d bytes", record.getCompressedSize()));
        if (record.getLocalFileHeaderOffs() != ExtraField.NO_DATA)
            printLine(out, "  original relative offset of local header:", String.format("%1$d (0x%1$08X) bytes", record.getLocalFileHeaderOffs()));
        if (record.getDisk() != ExtraField.NO_DATA)
            printLine(out, String.format("  original part number of this part (%04X):", record.getDisk()), record.getDisk());

        return true;
    }

    @Override
    protected int getSignature() {
        return record.getSignature();
    }

    @Override
    protected String getTitle() {
        return "Zip64 Extended Information";
    }

    public static final class Builder {

        private Zip64.ExtendedInfo record;
        private Block block;
        private int offs;
        private int columnWidth;

        public Zip64ExtendedInfoView build() {
            Objects.requireNonNull(record, "'record' must not be null");
            Objects.requireNonNull(block, "'block' must not be null");
            return new Zip64ExtendedInfoView(this);
        }

        public Builder record(Zip64.ExtendedInfo record) {
            this.record = record == Zip64.ExtendedInfo.NULL ? null : record;
            return this;
        }

        public Builder block(Block block) {
            this.block = block == Block.NULL ? null : block;
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
