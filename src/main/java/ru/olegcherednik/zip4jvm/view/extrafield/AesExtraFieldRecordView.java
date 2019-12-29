package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.AesExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.view.CompressionMethodView;
import ru.olegcherednik.zip4jvm.view.BaseView;

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
            compressionMethodView(record, view, builder).print(out);
        });
    }

    private static CompressionMethodView compressionMethodView(AesExtraFieldRecord record, BaseView view,
            Builder<AesExtraFieldRecord, AesExtraFieldRecordView> builder) {
        CompressionMethod compressionMethod = record.getCompressionMethod();
        GeneralPurposeFlag generalPurposeFlag = builder.getGeneralPurposeFlag();
        int offs = view.getOffs() + 2;
        int columnWidth = view.getColumnWidth();
        return new CompressionMethodView(compressionMethod, generalPurposeFlag, offs, columnWidth);
    }

}
