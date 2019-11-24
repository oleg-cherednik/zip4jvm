package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.os.NtfsTimestampExtraField;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class NtfsTimestampExtraFieldView extends View implements IExtraFieldView {

    private final NtfsTimestampExtraField record;
    private final Block block;

    public static Builder builder() {
        return new Builder();
    }

    private NtfsTimestampExtraFieldView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        record = builder.record;
        block = builder.block;
    }

    @Override
    public boolean print(PrintStream out) {
        printValueLocation(out, String.format("(0x%04X) NTFS Timestamps:", record.getSignature()), block);
        printLine(out, "  - total tags:", String.valueOf(record.getTags().size()));

        for (NtfsTimestampExtraField.Tag tag : record.getTags()) {
            if (tag instanceof NtfsTimestampExtraField.OneTag)
                print((NtfsTimestampExtraField.OneTag)tag, out);
            else if (tag instanceof NtfsTimestampExtraField.UnknownTag)
                print((NtfsTimestampExtraField.UnknownTag)tag, out);
        }

        return true;
    }

    private void print(NtfsTimestampExtraField.OneTag tag, PrintStream out) {
        printLine(out, String.format("  (0x%04X) Tag1:", tag.getSignature()), String.format("%d bytes", tag.getSize()));
        printLine(out, "    Creation Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", tag.getCreationTime()));
        printLine(out, "    Last Modified Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", tag.getLastModificationTime()));
        printLine(out, "    Last Accessed Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", tag.getLastAccessTime()));
    }

    private void print(NtfsTimestampExtraField.UnknownTag tag, PrintStream out) {
        printLine(out, String.format("  (0x%04X) Unknown Tag:", tag.getSignature()), String.format("%d bytes", tag.getSize()));

        ByteArrayHexView.builder()
                        .buf(tag.getData())
                        .offs(offs)
                        .columnWidth(columnWidth).build().print(out);
    }

    @Override
    public int getSignature() {
        return record.getSignature();
    }

    @Override
    public String getTitle() {
        return "NTFS Timestamps";
    }

    public static final class Builder {

        private NtfsTimestampExtraField record;
        private Block block;
        private int offs;
        private int columnWidth;

        public NtfsTimestampExtraFieldView build() {
            Objects.requireNonNull(record, "'record' must not be null");
            Objects.requireNonNull(block, "'block' must not be null");
            return new NtfsTimestampExtraFieldView(this);
        }

        public Builder record(NtfsTimestampExtraField record) {
            this.record = record == NtfsTimestampExtraField.NULL ? null : record;
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
