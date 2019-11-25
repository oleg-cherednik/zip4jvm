package ru.olegcherednik.zip4jvm.view.extrafield;

import org.apache.commons.lang.StringUtils;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.os.InfoZipNewUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class InfoZipNewUnixExtraFieldRecordView extends ExtraFieldRecordView {

    private final InfoZipNewUnixExtraFieldRecord record;

    public static Builder builder() {
        return new Builder();
    }

    private InfoZipNewUnixExtraFieldRecordView(Builder builder) {
        super(builder.block, builder.offs, builder.columnWidth);
        record = builder.record;
    }

    @Override
    public boolean print(PrintStream out) {
        printValueLocation(out, String.format("(0x%04X) new InfoZIP Unix/OS2/NT:", record.getSignature()), block);

        InfoZipNewUnixExtraFieldRecord.Payload payload = record.getPayload();

        if (payload instanceof InfoZipNewUnixExtraFieldRecord.VersionOnePayload)
            print((InfoZipNewUnixExtraFieldRecord.VersionOnePayload)record.getPayload(), out);
        else if (payload instanceof InfoZipNewUnixExtraFieldRecord.VersionUnknownPayload)
            print((InfoZipNewUnixExtraFieldRecord.VersionUnknownPayload)record.getPayload(), out);

        // TODO add final else

        return true;
    }

    private void print(InfoZipNewUnixExtraFieldRecord.VersionOnePayload payload, PrintStream out) {
        printLine(out, "  version:", String.valueOf(payload.getVersion()));

        if (StringUtils.isNotBlank(payload.getUid()))
            printLine(out, "  User identifier (UID):", payload.getUid());
        if (StringUtils.isNotBlank(payload.getGid()))
            printLine(out, "  Group Identifier (GID):", payload.getGid());
    }

    private void print(InfoZipNewUnixExtraFieldRecord.VersionUnknownPayload payload, PrintStream out) {
        printLine(out, "  version:", String.format("%d (unknown)", payload.getVersion()));

        ByteArrayHexView.builder()
                        .buf(payload.getData())
                        .offs(offs)
                        .columnWidth(columnWidth).build().print(out);
    }

    @Override
    public int getSignature() {
        return record.getSignature();
    }

    @Override
    public String getTitle() {
        return "new InfoZIP Unix-OS2-NT";
    }

    @Override
    public String getFileName() {
        return String.format("(0x%04X)_new_InfoZIP_Unix_OS2_NT", record.getSignature());
    }

    public static final class Builder {

        private InfoZipNewUnixExtraFieldRecord record;
        private Block block;
        private int offs;
        private int columnWidth;

        public InfoZipNewUnixExtraFieldRecordView build() {
            Objects.requireNonNull(record, "'record' must not be null");
            Objects.requireNonNull(block, "'block' must not be null");
            return new InfoZipNewUnixExtraFieldRecordView(this);
        }

        public Builder record(InfoZipNewUnixExtraFieldRecord record) {
            this.record = record == InfoZipNewUnixExtraFieldRecord.NULL ? null : record;
            return this;
        }

        public Builder block(Block block) {
            this.block = block == Block.NULL ? null : block;
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
