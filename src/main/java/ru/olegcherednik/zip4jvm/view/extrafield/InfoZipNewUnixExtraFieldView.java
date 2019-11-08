package ru.olegcherednik.zip4jvm.view.extrafield;

import org.apache.commons.lang.StringUtils;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.os.InfoZipNewUnixExtraField;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class InfoZipNewUnixExtraFieldView extends View {

    private final InfoZipNewUnixExtraField record;
    private final Block block;

    public static Builder builder() {
        return new Builder();
    }

    private InfoZipNewUnixExtraFieldView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        record = builder.record;
        block = builder.block;
    }

    @Override
    public void print(PrintStream out) {
        printLine(out, String.format("(0x%04X) new InfoZIP Unix/OS2/NT: ", record.getSignature()), String.format("%d bytes", block.getSize()));
        printLine(out, "  - location:", String.format("%1$d (0x%1$08X) bytes", block.getOffs()));

        InfoZipNewUnixExtraField.Payload payload = record.getPayload();

        if (payload instanceof InfoZipNewUnixExtraField.VersionOnePayload)
            print((InfoZipNewUnixExtraField.VersionOnePayload)record.getPayload(), out);
        else if (payload instanceof InfoZipNewUnixExtraField.VersionUnknownPayload)
            print((InfoZipNewUnixExtraField.VersionUnknownPayload)record.getPayload(), out);
    }

    private void print(InfoZipNewUnixExtraField.VersionOnePayload payload, PrintStream out) {
        printLine(out, "  version:", String.format("%d", payload.getVersion()));

        if (StringUtils.isNotBlank(payload.getUid()))
            printLine(out, "  User identifier (UID):", String.format("%s", payload.getUid()));
        if (StringUtils.isNotBlank(payload.getGid()))
            printLine(out, "  Group Identifier (GID):", String.format("%s", payload.getGid()));
    }

    private void print(InfoZipNewUnixExtraField.VersionUnknownPayload payload, PrintStream out) {
        printLine(out, "  version:", String.format("%d (unknown)", payload.getVersion()));

        ByteArrayHexView.builder()
                        .buf(payload.getData())
                        .offs(offs)
                        .columnWidth(columnWidth).build().print(out);
    }

    public static final class Builder {

        private InfoZipNewUnixExtraField record;
        private Block block;
        private int offs;
        private int columnWidth;

        public InfoZipNewUnixExtraFieldView build() {
            return new InfoZipNewUnixExtraFieldView(this);
        }

        public Builder record(InfoZipNewUnixExtraField record) {
            this.record = record;
            return this;
        }

        public Builder block(Block block) {
            this.block = block;
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
