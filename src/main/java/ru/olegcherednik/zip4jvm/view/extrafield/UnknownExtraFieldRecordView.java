package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class UnknownExtraFieldRecordView extends ExtraFieldRecordView<ExtraField.Record> {

    public static BaseBuilder<ExtraField.Record, UnknownExtraFieldRecordView> builder() {
        return new BaseBuilder<>(UnknownExtraFieldRecordView::new);
    }

    private UnknownExtraFieldRecordView(BaseBuilder<ExtraField.Record, UnknownExtraFieldRecordView> builder) {
        super(builder, (record, view, out) -> ByteArrayHexView.builder()
                                                              .data(builder.data)
                                                              .offs(view.getOffs())
                                                              .columnWidth(view.getColumnWidth()).build().print(out));
    }
}

