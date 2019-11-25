package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class UnknownExtraFieldRecordView extends ExtraFieldRecordView {

    private final ExtraField.Record.Unknown record;

    public static Builder builder() {
        return new Builder();
    }

    private UnknownExtraFieldRecordView(Builder builder) {
        super(builder.block, builder.offs, builder.columnWidth);
        record = builder.record;
    }

    @Override
    public boolean print(PrintStream out) {
        printValueLocation(out, String.format("(0x%04X) Unknown:", record.getSignature()), block);

        ByteArrayHexView.builder()
                        .buf(record.getData())
                        .offs(offs)
                        .columnWidth(columnWidth).build().print(out);

        return true;
    }

    @Override
    public int getSignature() {
        return record.getSignature();
    }

    @Override
    public String getTitle() {
        return "Unknown";
    }

    public static final class Builder {

        private ExtraField.Record.Unknown record;
        private Block block;
        private int offs;
        private int columnWidth;

        public UnknownExtraFieldRecordView build() {
            Objects.requireNonNull(record, "'record' must not be null");
            Objects.requireNonNull(block, "'block' must not be null");
            return new UnknownExtraFieldRecordView(this);
        }

        public Builder record(ExtraField.Record.Unknown record) {
            this.record = record;
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
