package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.os.InfoZipOldUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

import static ru.olegcherednik.zip4jvm.model.ExtraField.NO_DATA;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class InfoZipOldUnixExtraFieldRecordView extends ExtraFieldRecordView<InfoZipOldUnixExtraFieldRecord> {

    public static Builder<InfoZipOldUnixExtraFieldRecord, InfoZipOldUnixExtraFieldRecordView> builder() {
        return new Builder<>(InfoZipOldUnixExtraFieldRecordView::new);
    }

    private InfoZipOldUnixExtraFieldRecordView(Builder<InfoZipOldUnixExtraFieldRecord, InfoZipOldUnixExtraFieldRecordView> builder) {
        super(builder, (record, view, out) -> {
            view.printLine(out, "  Last Modified Date:", ZipUtils.utcDateTime(record.getLastModificationTime()));
            view.printLine(out, "  Last Accessed Date:", ZipUtils.utcDateTime(record.getLastAccessTime()));

            if (record.getUid() != NO_DATA)
                view.printLine(out, "  User identifier (UID):", String.valueOf(record.getUid()));
            if (record.getGid() != NO_DATA)
                view.printLine(out, "  Group Identifier (GID):", String.valueOf(record.getGid()));
        });
    }

}
