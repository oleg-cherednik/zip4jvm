package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.Flags;
import ru.olegcherednik.zip4jvm.model.extrafield.AlgIdExtraFieldRecord;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;

/**
 * @author Oleg Cherednik
 * @since 16.02.2020
 */
final class AlgIdExtraFieldRecordView extends ExtraFieldRecordView<AlgIdExtraFieldRecord> {

    public static Builder<AlgIdExtraFieldRecord, AlgIdExtraFieldRecordView> builder() {
        return new Builder<>(AlgIdExtraFieldRecordView::new);
    }

    private AlgIdExtraFieldRecordView(Builder<AlgIdExtraFieldRecord, AlgIdExtraFieldRecordView> builder) {
        super(builder, (record, view, out) -> {
            EncryptionAlgorithm encryptionAlgorithm = record.getEncryptionAlgorithm();
            Flags flags = record.getFlags();

            view.printLine(out, "  format:", record.getFormat());
            view.printLine(out, String.format("  encryption algorithm (0x%04X):", encryptionAlgorithm.getCode()), encryptionAlgorithm.getTitle());
            view.printLine(out, "  encryption key bits:", record.getBitLength());
            view.printLine(out, String.format("  flags (0x%02X):", flags.getCode()), flags.getTitle());
            view.printLine(out, "  unknown data length:", String.format("%d bytes", record.getUnknown().length));
            new ByteArrayHexView(record.getUnknown(), view.getOffs() + 4, view.getColumnWidth()).print(out);
        });
    }

}
