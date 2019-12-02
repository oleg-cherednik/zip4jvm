package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.utils.function.PrintFoo;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 24.11.2019
 */
public abstract class ExtraFieldRecordView<R extends ExtraField.Record> extends View {

    protected final R record;
    protected final Block block;
    private final PrintFoo<R, View> consumer;

    protected ExtraFieldRecordView(BaseBuilder<?, R, ?> builder, PrintFoo<R, View> consumer) {
        super(builder.offs, builder.columnWidth);
        record = builder.record;
        block = builder.block;
        this.consumer = consumer;
    }

    protected int getSignature() {
        return record.getSignature();
    }

    protected String getTitle() {
        return record.getTitle();
    }

    public String getFileName() {
        String title = getTitle();
        title = title.replaceAll(" ", "_");
        title = title.replaceAll("[/\\\\]", "-");
        return String.format("(0x%04X)_%s", getSignature(), title);
    }

    @Override
    public boolean print(PrintStream out) {
        printValueLocation(out, String.format("(0x%04X) %s:", getSignature(), getTitle()), block);
        consumer.print(record, this, out);
        return true;
    }

    protected abstract static class BaseBuilder<T, R extends ExtraField.Record, V extends ExtraFieldRecordView<R>> {

        protected final Function<T, V> sup;
        protected R record;
        protected Block block;
        protected int offs;
        protected int columnWidth;

        protected BaseBuilder() {
            sup = null;
        }

        protected BaseBuilder(Function<T, V> sup) {
            this.sup = sup;
        }

        public V build() {
            check();
            return sup.apply((T)this);
        }

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
