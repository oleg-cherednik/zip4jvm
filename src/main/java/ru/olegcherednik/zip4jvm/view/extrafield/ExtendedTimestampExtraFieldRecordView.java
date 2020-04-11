package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.extrafield.ExtendedTimestampExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class ExtendedTimestampExtraFieldRecordView extends ExtraFieldRecordView<ExtendedTimestampExtraFieldRecord> {

    public static Builder<ExtendedTimestampExtraFieldRecord, ExtendedTimestampExtraFieldRecordView> builder() {
        return new Builder<>(ExtendedTimestampExtraFieldRecordView::new);
    }

    private ExtendedTimestampExtraFieldRecordView(Builder<ExtendedTimestampExtraFieldRecord, ExtendedTimestampExtraFieldRecordView> builder) {
        super(builder, (record, view, out) -> {
            if (record.getFlag().isLastModificationTime() && record.getLastModificationTime() >= 0)
                view.printLine(out, "  Last Modified Date:", ZipUtils.utcDateTime(record.getLastModificationTime()));
            if (record.getFlag().isLastAccessTime() && record.getLastAccessTime() >= 0)
                view.printLine(out, "  Last Accessed Date:", ZipUtils.utcDateTime(record.getLastAccessTime()));
            if (record.getFlag().isCreationTime() && record.getCreationTime() >= 0)
                view.printLine(out, "  Creation Date:", ZipUtils.utcDateTime(record.getCreationTime()));
        });
    }

}
