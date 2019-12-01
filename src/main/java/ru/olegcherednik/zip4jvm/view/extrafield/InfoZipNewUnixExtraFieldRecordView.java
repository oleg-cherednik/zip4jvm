package ru.olegcherednik.zip4jvm.view.extrafield;

import org.apache.commons.lang.StringUtils;
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
        super(builder.block, builder.file, builder.offs, builder.columnWidth);
        record = builder.record;
    }

    @Override
    public boolean print(PrintStream out) {
        super.print(out);

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
    protected int getSignature() {
        return record.getSignature();
    }

    @Override
    protected String getTitle() {
        return "new InfoZIP Unix/OS2/NT";
    }

    public static final class Builder extends BaseBuilder<Builder> {

        private InfoZipNewUnixExtraFieldRecord record;

        public InfoZipNewUnixExtraFieldRecordView build() {
            check();
            return new InfoZipNewUnixExtraFieldRecordView(this);
        }

        @Override
        protected void check() {
            Objects.requireNonNull(record, "'record' must not be null");
        }

        public Builder record(InfoZipNewUnixExtraFieldRecord record) {
            this.record = record == InfoZipNewUnixExtraFieldRecord.NULL ? null : record;
            return this;
        }

    }
}
