package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.os.InfoZipOldUnixExtraFieldRecord;

import java.io.PrintStream;
import java.util.Objects;

import static ru.olegcherednik.zip4jvm.model.ExtraField.NO_DATA;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class InfoZipOldUnixExtraFieldRecordView extends ExtraFieldRecordView {

    private final InfoZipOldUnixExtraFieldRecord record;

    public static Builder builder() {
        return new Builder();
    }

    private InfoZipOldUnixExtraFieldRecordView(Builder builder) {
        super(builder.block, builder.file, builder.offs, builder.columnWidth);
        record = builder.record;
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

    @Override
    protected int getSignature() {
        return record.getSignature();
    }

    @Override
    protected String getTitle() {
        return "old InfoZIP Unix/OS2/NT";
    }

    public static final class Builder extends BaseBuilder<Builder> {

        private InfoZipOldUnixExtraFieldRecord record;

        public InfoZipOldUnixExtraFieldRecordView build() {
            check();
            return new InfoZipOldUnixExtraFieldRecordView(this);
        }

        public Builder record(InfoZipOldUnixExtraFieldRecord record) {
            this.record = record == InfoZipOldUnixExtraFieldRecord.NULL ? null : record;
            return this;
        }

        @Override
        protected void check() {
            Objects.requireNonNull(record, "'record' must not be null");
        }

    }
}
