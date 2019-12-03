package ru.olegcherednik.zip4jvm.view.extrafield;

import lombok.Getter;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.utils.function.PrintFoo;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 24.11.2019
 */
public abstract class ExtraFieldRecordView<R extends ExtraField.Record> extends View {

    protected final R record;
    protected final Block block;
    private final PrintFoo<R, View> consumer;

    protected ExtraFieldRecordView(Builder<R, ?> builder, PrintFoo<R, View> consumer) {
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

    @Getter
    public static class Builder<R extends ExtraField.Record, V extends ExtraFieldRecordView<R>> {

        private final Function<Builder<R, V>, V> sup;
        private R record;
        private GeneralPurposeFlag generalPurposeFlag;
        private byte[] data = ArrayUtils.EMPTY_BYTE_ARRAY;
        private Block block;
        private int offs;
        private int columnWidth;

        protected Builder(Function<Builder<R, V>, V> sup) {
            this.sup = sup;
        }

        public V build() {
            check();
            return sup.apply(this);
        }

        protected void check() {
            Objects.requireNonNull(record, "'record' must not be null");
            Objects.requireNonNull(block, "'block' must not be null");
        }

        public final Builder<R, V> record(R record) {
            this.record = record == null || record.isNull() ? null : record;
            return this;
        }

        public final Builder<R, V> generalPurposeFlag(GeneralPurposeFlag generalPurposeFlag) {
            this.generalPurposeFlag = generalPurposeFlag;
            return this;
        }

        @SuppressWarnings("MethodCanBeVariableArityMethod")
        public Builder<R, V> data(byte[] data) {
            this.data = Optional.ofNullable(data).orElse(ArrayUtils.EMPTY_BYTE_ARRAY);
            return this;
        }

        public final Builder<R, V> block(Block block) {
            this.block = block == Block.NULL ? null : block;
            return this;
        }

        public final Builder<R, V> offs(int offs) {
            this.offs = offs;
            return this;
        }

        public final Builder<R, V> columnWidth(int columnWidth) {
            this.columnWidth = columnWidth;
            return this;
        }
    }
}
