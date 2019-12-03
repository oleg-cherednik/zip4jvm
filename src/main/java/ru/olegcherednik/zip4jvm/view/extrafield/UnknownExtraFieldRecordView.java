package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;

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


        private Builder() {
            super(UnknownExtraFieldRecordView::new);
        }


    }
}
