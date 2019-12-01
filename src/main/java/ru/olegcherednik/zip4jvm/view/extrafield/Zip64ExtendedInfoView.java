package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.Zip64;

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
        super(builder.block, builder.file, builder.offs, builder.columnWidth);
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

    public static final class Builder extends BaseBuilder<Builder> {

        private Zip64.ExtendedInfo record;

        public Zip64ExtendedInfoView build() {
            check();
            return new Zip64ExtendedInfoView(this);
        }

        @Override
        protected void check() {
            super.check();
            Objects.requireNonNull(record, "'record' must not be null");
        }

        public Builder record(Zip64.ExtendedInfo record) {
            this.record = record == Zip64.ExtendedInfo.NULL ? null : record;
            return this;
        }

    }
}
