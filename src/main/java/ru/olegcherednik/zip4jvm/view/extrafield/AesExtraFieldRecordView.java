package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.AesExtraFieldRecord;
import ru.olegcherednik.zip4jvm.view.CompressionMethodView;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class AesExtraFieldRecordView extends ExtraFieldRecordView<AesExtraFieldRecord> {

    public static Builder<AesExtraFieldRecord, AesExtraFieldRecordView> builder() {
        return new Builder<>(AesExtraFieldRecordView::new);
    }

    private AesExtraFieldRecordView(Builder<AesExtraFieldRecord, AesExtraFieldRecordView> builder) {
        super(builder, (record, view, out) -> {
            view.printLine(out, "  Encryption Tag Version:", String.format("%s-%d", record.getVendor(), record.getVersionNumber()));
            view.printLine(out, "  Encryption Key Bits:", record.getStrength().getSize());

            CompressionMethodView.builder()
                                 .compressionMethod(record.getCompressionMethod())
                                 .generalPurposeFlag(builder.getGeneralPurposeFlag())
                                 .offs(view.getOffs() + 2)
                                 .columnWidth(view.getColumnWidth()).build().print(out);
        });
    }

}
