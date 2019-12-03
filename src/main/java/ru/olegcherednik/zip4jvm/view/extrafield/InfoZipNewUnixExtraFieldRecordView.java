package ru.olegcherednik.zip4jvm.view.extrafield;

import org.apache.commons.lang.StringUtils;
import ru.olegcherednik.zip4jvm.model.os.InfoZipNewUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.function.PrintFoo;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class InfoZipNewUnixExtraFieldRecordView extends ExtraFieldRecordView<InfoZipNewUnixExtraFieldRecord> {

    public static Builder<InfoZipNewUnixExtraFieldRecord, InfoZipNewUnixExtraFieldRecordView> builder() {
        return new Builder<>(InfoZipNewUnixExtraFieldRecordView::new);
    }

    private InfoZipNewUnixExtraFieldRecordView(Builder<InfoZipNewUnixExtraFieldRecord, InfoZipNewUnixExtraFieldRecordView> builder) {
        super(builder, new PrintFoo<InfoZipNewUnixExtraFieldRecord, View>() {
            @Override
            public void print(InfoZipNewUnixExtraFieldRecord record, View view, PrintStream out) {
                InfoZipNewUnixExtraFieldRecord.Payload payload = record.getPayload();

                if (payload instanceof InfoZipNewUnixExtraFieldRecord.VersionOnePayload)
                    print((InfoZipNewUnixExtraFieldRecord.VersionOnePayload)record.getPayload(), view, out);
                else if (payload instanceof InfoZipNewUnixExtraFieldRecord.VersionUnknownPayload)
                    print((InfoZipNewUnixExtraFieldRecord.VersionUnknownPayload)record.getPayload(), view, out);

                // TODO add final else
            }

            private void print(InfoZipNewUnixExtraFieldRecord.VersionOnePayload payload, View view, PrintStream out) {
                view.printLine(out, "  version:", String.valueOf(payload.getVersion()));

                if (StringUtils.isNotBlank(payload.getUid()))
                    view.printLine(out, "  User identifier (UID):", payload.getUid());
                if (StringUtils.isNotBlank(payload.getGid()))
                    view.printLine(out, "  Group Identifier (GID):", payload.getGid());
            }

            private void print(InfoZipNewUnixExtraFieldRecord.VersionUnknownPayload payload, View view, PrintStream out) {
                view.printLine(out, "  version:", String.format("%d (unknown)", payload.getVersion()));

                ByteArrayHexView.builder()
                                .data(payload.getData())
                                .offs(view.getOffs())
                                .columnWidth(view.getColumnWidth()).build().print(out);
            }
        });
    }
}

