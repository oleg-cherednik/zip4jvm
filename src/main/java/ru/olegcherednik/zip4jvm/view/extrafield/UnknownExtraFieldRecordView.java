package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class UnknownExtraFieldRecordView extends ExtraFieldRecordView<ExtraField.Record> {

    public static Builder<ExtraField.Record, UnknownExtraFieldRecordView> builder() {
        return new Builder<>(UnknownExtraFieldRecordView::new);
    }

    private UnknownExtraFieldRecordView(Builder<ExtraField.Record, UnknownExtraFieldRecordView> builder) {
        super(builder, (record, view, out) -> ByteArrayHexView.builder()
                                                              .data(builder.getData())
                                                              .position(view.getOffs(), view.getColumnWidth()).build().print(out));
    }
}

