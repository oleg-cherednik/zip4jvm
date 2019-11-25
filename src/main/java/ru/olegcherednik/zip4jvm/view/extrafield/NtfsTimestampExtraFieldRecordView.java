package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.os.NtfsTimestampExtraFieldRecord;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;

import java.io.PrintStream;
import java.util.Objects;
import java.util.Optional;

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
        super(builder.block, builder.offs, builder.columnWidth);
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
                        .buf(tag.getData())
                        .offs(offs)
                        .columnWidth(columnWidth).build().print(out);
    }

    @Override
    protected int getSignature() {
        return record.getSignature();
    }

    @Override
    protected String getTitle() {
        return "NTFS Timestamps";
    }

    public static final class Builder {

        private NtfsTimestampExtraFieldRecord record;
        private Block block;
        private int offs;
        private int columnWidth;

        public NtfsTimestampExtraFieldRecordView build() {
            Objects.requireNonNull(record, "'record' must not be null");
            Objects.requireNonNull(block, "'block' must not be null");
            return new NtfsTimestampExtraFieldRecordView(this);
        }

        public Builder record(NtfsTimestampExtraFieldRecord record) {
            this.record = record == NtfsTimestampExtraFieldRecord.NULL ? null : record;
            return this;
        }

        public Builder block(Block block) {
            this.block = Optional.ofNullable(block).orElse(Block.NULL);
            return this;
        }

        public Builder offs(int offs) {
            this.offs = offs;
            return this;
        }

        public Builder columnWidth(int columnWidth) {
            this.columnWidth = columnWidth;
            return this;
        }
    }
}
