package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.os.ExtendedTimestampExtraFieldRecord;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class ExtendedTimestampExtraFieldRecordView extends ExtraFieldRecordView<ExtendedTimestampExtraFieldRecord> {

    public static Builder builder() {
        return new Builder();
    }

    private ExtendedTimestampExtraFieldRecordView(Builder builder) {
        super(builder, (record, view, out) -> {
            if (record.getFlag().isLastModificationTime())
                view.printLine(out, "  Last Modified Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", record.getLastModificationTime()));
            if (record.getFlag().isLastAccessTime())
                view.printLine(out, "  Last Accessed Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", record.getLastAccessTime()));
            if (record.getFlag().isCreationTime())
                view.printLine(out, "  Creation Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", record.getCreationTime()));
        });
    }

    public static final class Builder extends BaseBuilder<Builder, ExtendedTimestampExtraFieldRecord, ExtendedTimestampExtraFieldRecordView> {

        private Builder() {
            super(ExtendedTimestampExtraFieldRecordView::new);
        }

    }
}
