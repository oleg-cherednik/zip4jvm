package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.os.ExtendedTimestampExtraField;
import ru.olegcherednik.zip4jvm.view.IView;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class ExtendedTimestampExtraFieldView extends View {

    private final ExtendedTimestampExtraField record;
    private final Block block;

    public static Builder builder() {
        return new Builder();
    }

    private ExtendedTimestampExtraFieldView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        record = builder.record;
        block = builder.block;
    }

    @Override
    public boolean print(PrintStream out) {
        if (record.isNull() || block == Block.NULL)
            return false;

        printValueLocation(out, String.format("(0x%04X) Universal time:", record.getSignature()), block);

        if (record.getFlag().isLastModificationTime())
            printLine(out, "  Last Modified Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", record.getLastModificationTime()));
        if (record.getFlag().isLastAccessTime())
            printLine(out, "  Last Accessed Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", record.getLastAccessTime()));
        if (record.getFlag().isCreationTime())
            printLine(out, "  Creation Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", record.getCreationTime()));

        return true;
    }

    public static final class Builder {

        private ExtendedTimestampExtraField record = ExtendedTimestampExtraField.NULL;
        private Block block = Block.NULL;
        private int offs;
        private int columnWidth;

        public IView build() {
            return record.isNull() || block == Block.NULL ? IView.NULL : new ExtendedTimestampExtraFieldView(this);
        }

        public Builder record(ExtendedTimestampExtraField record) {
            this.record = Optional.ofNullable(record).orElse(ExtendedTimestampExtraField.NULL);
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
