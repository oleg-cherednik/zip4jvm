package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.AesExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.view.CompressionMethodView;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class AesExtraFieldRecordView extends ExtraFieldRecordView {

    private final AesExtraFieldRecord record;
    private final GeneralPurposeFlag generalPurposeFlag;

    public static Builder builder() {
        return new Builder();
    }

    private AesExtraFieldRecordView(Builder builder) {
        super(builder);
        record = builder.record;
        generalPurposeFlag = builder.generalPurposeFlag;
    }

    @Override
    public boolean print(PrintStream out) {
        super.print(out);

        printLine(out, "  Encryption Tag Version:", String.format("%s-%d", record.getVendor(), record.getVersionNumber()));
        printLine(out, "  Encryption Key Bits:", record.getStrength().getSize());

        CompressionMethodView.builder()
                             .compressionMethod(record.getCompressionMethod())
                             .generalPurposeFlag(generalPurposeFlag)
                             .offs(offs + 2)
                             .columnWidth(columnWidth).build().print(out);

        return true;
    }

    @Override
    protected int getSignature() {
        return record.getSignature();
    }

    @Override
    protected String getTitle() {
        return "AES Encryption Tag";
    }

    public static final class Builder extends BaseBuilder<Builder> {

        private AesExtraFieldRecord record;
        private GeneralPurposeFlag generalPurposeFlag;

        public AesExtraFieldRecordView build() {
            check();
            return new AesExtraFieldRecordView(this);
        }

        @Override
        protected void check() {
            super.check();
            Objects.requireNonNull(record, "'record' must not be null");
        }

        public Builder record(AesExtraFieldRecord record) {
            this.record = record == AesExtraFieldRecord.NULL ? null : record;
            return this;
        }

        public Builder generalPurposeFlag(GeneralPurposeFlag generalPurposeFlag) {
            this.generalPurposeFlag = generalPurposeFlag;
            return this;
        }

    }
}
