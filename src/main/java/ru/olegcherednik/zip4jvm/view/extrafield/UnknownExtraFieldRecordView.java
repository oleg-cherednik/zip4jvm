package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class UnknownExtraFieldRecordView extends ExtraFieldRecordView {

    private final ExtraField.Record record;

    public static Builder builder() {
        return new Builder();
    }

    private UnknownExtraFieldRecordView(Builder builder) {
        super(builder.block, builder.file, builder.offs, builder.columnWidth);
        record = builder.record;
    }

    @Override
    public boolean print(PrintStream out) {
        printValueLocation(out, String.format("(0x%04X) Unknown:", record.getSignature()), block);

        ByteArrayHexView.builder()
                        .buf(block.getData())
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

    public static final class Builder extends BaseBuilder<Builder> {

        private ExtraField.Record record;

        public UnknownExtraFieldRecordView build() {
            check();
            return new UnknownExtraFieldRecordView(this);
        }

        @Override
        protected void check() {
            super.check();
            Objects.requireNonNull(record, "'record' must not be null");
        }

        public Builder record(ExtraField.Record record) {
            this.record = record;
            return this;
        }

    }
}
