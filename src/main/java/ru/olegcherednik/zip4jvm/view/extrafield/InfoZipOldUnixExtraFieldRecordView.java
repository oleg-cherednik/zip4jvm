package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.os.InfoZipOldUnixExtraFieldRecord;

import static ru.olegcherednik.zip4jvm.model.ExtraField.NO_DATA;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class InfoZipOldUnixExtraFieldRecordView extends ExtraFieldRecordView<InfoZipOldUnixExtraFieldRecord> {

    public static BaseBuilder<InfoZipOldUnixExtraFieldRecord, InfoZipOldUnixExtraFieldRecordView> builder() {
        return new BaseBuilder<>(InfoZipOldUnixExtraFieldRecordView::new);
    }

    private InfoZipOldUnixExtraFieldRecordView(BaseBuilder<InfoZipOldUnixExtraFieldRecord, InfoZipOldUnixExtraFieldRecordView> builder) {
        super(builder, (record, view, out) -> {
            view.printLine(out, "  Last Modified Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", record.getLastModificationTime()));
            view.printLine(out, "  Last Accessed Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", record.getLastAccessTime()));

            if (record.getUid() != NO_DATA)
                view.printLine(out, "  User identifier (UID):", String.valueOf(record.getUid()));
            if (record.getGid() != NO_DATA)
                view.printLine(out, "  Group Identifier (GID):", String.valueOf(record.getGid()));
        });
    }

}
