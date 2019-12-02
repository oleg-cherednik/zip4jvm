package ru.olegcherednik.zip4jvm.view.extrafield;

import org.apache.commons.lang.StringUtils;
import ru.olegcherednik.zip4jvm.model.os.InfoZipNewUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class InfoZipNewUnixExtraFieldRecordView extends ExtraFieldRecordView<InfoZipNewUnixExtraFieldRecord> {

    public static Builder builder() {
        return new Builder();
    }

    private InfoZipNewUnixExtraFieldRecordView(Builder builder) {
        super(builder);
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
                        .data(payload.getData())
                        .offs(offs)
                        .columnWidth(columnWidth).build().print(out);
    }

    public static final class Builder extends BaseBuilder<Builder, InfoZipNewUnixExtraFieldRecord> {

        public InfoZipNewUnixExtraFieldRecordView build() {
            check();
            return new InfoZipNewUnixExtraFieldRecordView(this);
        }

    }
}
