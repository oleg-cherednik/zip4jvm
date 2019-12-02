package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.AesExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.view.CompressionMethodView;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class AesExtraFieldRecordView extends ExtraFieldRecordView<AesExtraFieldRecord> {

    public static Builder builder() {
        return new Builder();
    }

    private AesExtraFieldRecordView(Builder builder) {
        super(builder, (record, view, out) -> {
            view.printLine(out, "  Encryption Tag Version:", String.format("%s-%d", record.getVendor(), record.getVersionNumber()));
            view.printLine(out, "  Encryption Key Bits:", record.getStrength().getSize());

            CompressionMethodView.builder()
                                 .compressionMethod(record.getCompressionMethod())
                                 .generalPurposeFlag(builder.generalPurposeFlag)
                                 .offs(view.getOffs() + 2)
                                 .columnWidth(view.getColumnWidth()).build().print(out);
        });
    }

    public static final class Builder extends BaseBuilder<Builder, AesExtraFieldRecord, AesExtraFieldRecordView> {

        private GeneralPurposeFlag generalPurposeFlag;

        private Builder() {
            super(AesExtraFieldRecordView::new);
        }

        public Builder generalPurposeFlag(GeneralPurposeFlag generalPurposeFlag) {
            this.generalPurposeFlag = generalPurposeFlag;
            return this;
        }

    }
}
