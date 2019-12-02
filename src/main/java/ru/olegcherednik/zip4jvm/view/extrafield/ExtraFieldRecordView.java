package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 24.11.2019
 */
public abstract class ExtraFieldRecordView extends View {

    protected final Block block;

    protected ExtraFieldRecordView(BaseBuilder<?, ?> builder) {
        super(builder.offs, builder.columnWidth);
        block = builder.block;
    }

    protected abstract int getSignature();

    protected abstract String getTitle();

    public String getFileName() {
        String title = getTitle();
        title = title.replaceAll(" ", "_");
        title = title.replaceAll("[/\\\\]", "-");
        return String.format("(0x%04X)_%s", getSignature(), title);
    }

    @Override
    public boolean print(PrintStream out) {
        printValueLocation(out, String.format("(0x%04X) %s:", getSignature(), getTitle()), block);
        return true;
    }

    protected abstract static class BaseBuilder<T extends BaseBuilder<?, ?>, R extends ExtraField.Record> {

        protected R record;
        protected Block block;
        protected int offs;
        protected int columnWidth;

        protected void check() {
//            Objects.requireNonNull(record, "'record' must not be null");
            Objects.requireNonNull(block, "'block' must not be null");
        }

        public final T record(R record) {
            this.record = record == null || record.isNull() ? null : record;
            return (T)this;
        }

        public final T block(Block block) {
            this.block = block == Block.NULL ? null : block;
            return (T)this;
        }

        public final T offs(int offs) {
            this.offs = offs;
            return (T)this;
        }

        public final T columnWidth(int columnWidth) {
            this.columnWidth = columnWidth;
            return (T)this;
        }
    }
}
