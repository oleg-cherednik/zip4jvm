package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.os.InfoZipOldUnixExtraFieldRecord;

import java.io.PrintStream;

import static ru.olegcherednik.zip4jvm.model.ExtraField.NO_DATA;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class InfoZipOldUnixExtraFieldRecordView extends ExtraFieldRecordView<InfoZipOldUnixExtraFieldRecord> {

    public static Builder builder() {
        return new Builder();
    }

    private InfoZipOldUnixExtraFieldRecordView(Builder builder) {
        super(builder);
    }

    @Override
    public boolean print(PrintStream out) {
        super.print(out);

        printLine(out, "  Last Modified Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", record.getLastModificationTime()));
        printLine(out, "  Last Accessed Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", record.getLastAccessTime()));

        if (record.getUid() != NO_DATA)
            printLine(out, "  User identifier (UID):", String.valueOf(record.getUid()));
        if (record.getGid() != NO_DATA)
            printLine(out, "  Group Identifier (GID):", String.valueOf(record.getGid()));

        return true;
    }

    public static final class Builder extends BaseBuilder<Builder, InfoZipOldUnixExtraFieldRecord, InfoZipOldUnixExtraFieldRecordView> {

        private Builder() {
            super(InfoZipOldUnixExtraFieldRecordView::new);
        }

    }
}
