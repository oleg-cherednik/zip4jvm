package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.os.NtfsTimestampExtraFieldRecord;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class NtfsTimestampExtraFieldRecordView extends ExtraFieldRecordView {

    private final NtfsTimestampExtraFieldRecord record;

    public static Builder builder() {
        return new Builder();
    }

    private NtfsTimestampExtraFieldRecordView(Builder builder) {
        super(builder);
        record = builder.record;
    }

    @Override
    public boolean print(PrintStream out) {
        super.print(out);

        printLine(out, "  - total tags:", String.valueOf(record.getTags().size()));

        for (NtfsTimestampExtraFieldRecord.Tag tag : record.getTags()) {
            if (tag instanceof NtfsTimestampExtraFieldRecord.OneTag)
                print((NtfsTimestampExtraFieldRecord.OneTag)tag, out);
            else if (tag instanceof NtfsTimestampExtraFieldRecord.UnknownTag)
                print((NtfsTimestampExtraFieldRecord.UnknownTag)tag, out);
        }

        return true;
    }

    private void print(NtfsTimestampExtraFieldRecord.OneTag tag, PrintStream out) {
        printLine(out, String.format("  (0x%04X) Tag1:", tag.getSignature()), String.format("%d bytes", tag.getSize()));
        printLine(out, "    Creation Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", tag.getCreationTime()));
        printLine(out, "    Last Modified Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", tag.getLastModificationTime()));
        printLine(out, "    Last Accessed Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", tag.getLastAccessTime()));
    }

    private void print(NtfsTimestampExtraFieldRecord.UnknownTag tag, PrintStream out) {
        printLine(out, String.format("  (0x%04X) Unknown Tag:", tag.getSignature()), String.format("%d bytes", tag.getSize()));

        ByteArrayHexView.builder()
                        .data(tag.getData())
                        .offs(offs)
                        .columnWidth(columnWidth).build().print(out);
    }

    @Override
    protected int getSignature() {
        return record.getSignature();
    }

    @Override
    protected String getTitle() {
        return record.getTitle();
    }

    public static final class Builder extends BaseBuilder<Builder, NtfsTimestampExtraFieldRecord> {

        public NtfsTimestampExtraFieldRecordView build() {
            check();
            return new NtfsTimestampExtraFieldRecordView(this);
        }

    }
}
