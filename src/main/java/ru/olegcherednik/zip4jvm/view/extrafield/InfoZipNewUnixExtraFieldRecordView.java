package ru.olegcherednik.zip4jvm.view.extrafield;

import org.apache.commons.lang.StringUtils;
import ru.olegcherednik.zip4jvm.model.os.InfoZipNewUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.BaseView;

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
        super(builder, new PrintConsumer<InfoZipNewUnixExtraFieldRecord, BaseView>() {
            @Override
            public void print(InfoZipNewUnixExtraFieldRecord record, BaseView view, PrintStream out) {
                InfoZipNewUnixExtraFieldRecord.Payload payload = record.getPayload();

                if (payload instanceof InfoZipNewUnixExtraFieldRecord.VersionOnePayload)
                    print((InfoZipNewUnixExtraFieldRecord.VersionOnePayload)record.getPayload(), view, out);
                else if (payload instanceof InfoZipNewUnixExtraFieldRecord.VersionUnknownPayload)
                    print((InfoZipNewUnixExtraFieldRecord.VersionUnknownPayload)record.getPayload(), view, out);

                // TODO add final else
            }

            private void print(InfoZipNewUnixExtraFieldRecord.VersionOnePayload payload, BaseView view, PrintStream out) {
                view.printLine(out, "  version:", String.valueOf(payload.getVersion()));

                if (StringUtils.isNotBlank(payload.getUid()))
                    view.printLine(out, "  User identifier (UID):", payload.getUid());
                if (StringUtils.isNotBlank(payload.getGid()))
                    view.printLine(out, "  Group Identifier (GID):", payload.getGid());
            }

            private void print(InfoZipNewUnixExtraFieldRecord.VersionUnknownPayload payload, BaseView view, PrintStream out) {
                view.printLine(out, "  version:", String.format("%d (unknown)", payload.getVersion()));
                new ByteArrayHexView(payload.getData(), view.getOffs(), view.getColumnWidth()).print(out);
            }
        });
    }
}

