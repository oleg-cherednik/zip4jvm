package ru.olegcherednik.zip4jvm.view.extrafield;

import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;

import java.io.PrintStream;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class UnknownExtraFieldRecordView extends ExtraFieldRecordView<ExtraField.Record> {

    private final byte[] data;

    public static Builder builder() {
        return new Builder();
    }

    private UnknownExtraFieldRecordView(Builder builder) {
        super(builder);
        data = builder.data;
    }

    @Override
    public boolean print(PrintStream out) {
        super.print(out);

        ByteArrayHexView.builder()
                        .data(data)
                        .offs(offs)
                        .columnWidth(columnWidth).build().print(out);

        return true;
    }

    public static final class Builder extends BaseBuilder<Builder, ExtraField.Record> {

        private byte[] data = ArrayUtils.EMPTY_BYTE_ARRAY;

        public UnknownExtraFieldRecordView build() {
            check();
            return new UnknownExtraFieldRecordView(this);
        }

        @SuppressWarnings("MethodCanBeVariableArityMethod")
        public Builder data(byte[] data) {
            this.data = Optional.ofNullable(data).orElse(ArrayUtils.EMPTY_BYTE_ARRAY);
            return this;
        }

    }
}
