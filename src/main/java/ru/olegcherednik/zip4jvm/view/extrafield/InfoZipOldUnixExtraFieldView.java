package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.os.InfoZipOldUnixExtraField;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;

import static ru.olegcherednik.zip4jvm.model.ExtraField.NO_DATA;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class InfoZipOldUnixExtraFieldView extends View {

    private final InfoZipOldUnixExtraField record;
    private final Block block;

    public static Builder builder() {
        return new Builder();
    }

    private InfoZipOldUnixExtraFieldView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        record = builder.record;
        block = builder.block;
    }

    @Override
    public void print(PrintStream out) {
        printLine(out, String.format("(0x%04X) old InfoZIP Unix/OS2/NT:", record.getSignature()), String.format("%d bytes", block.getSize()));
        printLine(out, "  - location:", String.format("%1$d (0x%1$08X) bytes", block.getOffs()));
        printLine(out, "  Last Modified Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", record.getLastModificationTime()));
        printLine(out, "  Last Accessed Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", record.getLastAccessTime()));

        if (record.getUid() != NO_DATA)
            printLine(out, "  User identifier (UID):", String.format("%d", record.getUid()));
        if (record.getGid() != NO_DATA)
            printLine(out, "  Group Identifier (GID):", String.format("%d", record.getGid()));
    }

    public static final class Builder {

        private InfoZipOldUnixExtraField record;
        private Block block;
        private int offs;
        private int columnWidth;

        public InfoZipOldUnixExtraFieldView build() {
            return new InfoZipOldUnixExtraFieldView(this);
        }

        public Builder record(InfoZipOldUnixExtraField record) {
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
