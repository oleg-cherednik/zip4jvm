package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.os.ExtendedTimestampExtraFieldRecord;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class ExtendedTimestampExtraFieldRecordView extends ExtraFieldRecordView {

    private final ExtendedTimestampExtraFieldRecord record;

    public static Builder builder() {
        return new Builder();
    }

    private ExtendedTimestampExtraFieldRecordView(Builder builder) {
        super(builder);
        record = builder.record;
    }

    @Override
    public boolean print(PrintStream out) {
        super.print(out);

        if (record.getFlag().isLastModificationTime())
            printLine(out, "  Last Modified Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", record.getLastModificationTime()));
        if (record.getFlag().isLastAccessTime())
            printLine(out, "  Last Accessed Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", record.getLastAccessTime()));
        if (record.getFlag().isCreationTime())
            printLine(out, "  Creation Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", record.getCreationTime()));

        return true;
    }

    @Override
    protected int getSignature() {
        return record.getSignature();
    }

    @Override
    protected String getTitle() {
        return "Universal time";
    }

    public static final class Builder extends BaseBuilder<Builder> {

        private ExtendedTimestampExtraFieldRecord record;

        public ExtendedTimestampExtraFieldRecordView build() {
            check();
            return new ExtendedTimestampExtraFieldRecordView(this);
        }

        @Override
        protected void check() {
            super.check();
            Objects.requireNonNull(record, "'record' must not be null");
        }

        public Builder record(ExtendedTimestampExtraFieldRecord record) {
            this.record = record == ExtendedTimestampExtraFieldRecord.NULL ? null : record;
            return this;
        }

    }
}
