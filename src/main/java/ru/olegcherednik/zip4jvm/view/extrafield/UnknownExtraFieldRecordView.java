package ru.olegcherednik.zip4jvm.view.extrafield;

import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;

import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class UnknownExtraFieldRecordView extends ExtraFieldRecordView<ExtraField.Record> {

    public static Builder builder() {
        return new Builder();
    }

    private UnknownExtraFieldRecordView(Builder builder) {
        super(builder, (record, view, out) -> ByteArrayHexView.builder()
                                                              .data(builder.data)
                                                              .offs(view.getOffs())
                                                              .columnWidth(view.getColumnWidth()).build().print(out));
    }

    public static final class Builder extends BaseBuilder<Builder, ExtraField.Record, UnknownExtraFieldRecordView> {

        private byte[] data = ArrayUtils.EMPTY_BYTE_ARRAY;

        private Builder() {
            super(UnknownExtraFieldRecordView::new);
        }

        @SuppressWarnings("MethodCanBeVariableArityMethod")
        public Builder data(byte[] data) {
            this.data = Optional.ofNullable(data).orElse(ArrayUtils.EMPTY_BYTE_ARRAY);
            return this;
        }

    }
}
