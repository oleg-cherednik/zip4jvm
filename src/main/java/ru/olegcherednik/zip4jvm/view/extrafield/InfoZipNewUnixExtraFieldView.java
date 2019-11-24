package ru.olegcherednik.zip4jvm.view.extrafield;

import org.apache.commons.lang.StringUtils;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.os.InfoZipNewUnixExtraField;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class InfoZipNewUnixExtraFieldView extends View implements IExtraFieldView {

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
    public boolean print(PrintStream out) {
        printValueLocation(out, String.format("(0x%04X) new InfoZIP Unix/OS2/NT:", record.getSignature()), block);

        InfoZipNewUnixExtraField.Payload payload = record.getPayload();

        if (payload instanceof InfoZipNewUnixExtraField.VersionOnePayload)
            print((InfoZipNewUnixExtraField.VersionOnePayload)record.getPayload(), out);
        else if (payload instanceof InfoZipNewUnixExtraField.VersionUnknownPayload)
            print((InfoZipNewUnixExtraField.VersionUnknownPayload)record.getPayload(), out);

        // TODO add final else

        return true;
    }

    private void print(InfoZipNewUnixExtraField.VersionOnePayload payload, PrintStream out) {
        printLine(out, "  version:", String.valueOf(payload.getVersion()));

        if (StringUtils.isNotBlank(payload.getUid()))
            printLine(out, "  User identifier (UID):", payload.getUid());
        if (StringUtils.isNotBlank(payload.getGid()))
            printLine(out, "  Group Identifier (GID):", payload.getGid());
    }

    private void print(InfoZipNewUnixExtraField.VersionUnknownPayload payload, PrintStream out) {
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

        private InfoZipNewUnixExtraField record;
        private Block block;
        private int offs;
        private int columnWidth;

        public InfoZipNewUnixExtraFieldView build() {
            Objects.requireNonNull(record, "'record' must not be null");
            Objects.requireNonNull(block, "'block' must not be null");
            return new InfoZipNewUnixExtraFieldView(this);
        }

        public Builder record(InfoZipNewUnixExtraField record) {
            this.record = record == InfoZipNewUnixExtraField.NULL ? null : record;
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
