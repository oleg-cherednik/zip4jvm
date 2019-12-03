package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.os.NtfsTimestampExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.function.PrintFoo;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class NtfsTimestampExtraFieldRecordView extends ExtraFieldRecordView<NtfsTimestampExtraFieldRecord> {

    public static BaseBuilder<NtfsTimestampExtraFieldRecord, NtfsTimestampExtraFieldRecordView> builder() {
        return new BaseBuilder<>(NtfsTimestampExtraFieldRecordView::new);
    }

    private NtfsTimestampExtraFieldRecordView(BaseBuilder<NtfsTimestampExtraFieldRecord, NtfsTimestampExtraFieldRecordView> builder) {
        super(builder, new PrintFoo<NtfsTimestampExtraFieldRecord, View>() {
            @Override
            public void print(NtfsTimestampExtraFieldRecord record, View view, PrintStream out) {
                view.printLine(out, "  - total tags:", String.valueOf(record.getTags().size()));

                for (NtfsTimestampExtraFieldRecord.Tag tag : record.getTags()) {
                    if (tag instanceof NtfsTimestampExtraFieldRecord.OneTag)
                        print((NtfsTimestampExtraFieldRecord.OneTag)tag, view, out);
                    else if (tag instanceof NtfsTimestampExtraFieldRecord.UnknownTag)
                        print((NtfsTimestampExtraFieldRecord.UnknownTag)tag, view, out);
                }
            }

            private void print(NtfsTimestampExtraFieldRecord.OneTag tag, View view, PrintStream out) {
                view.printLine(out, String.format("  (0x%04X) Tag1:", tag.getSignature()), String.format("%d bytes", tag.getSize()));
                view.printLine(out, "    Creation Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", tag.getCreationTime()));
                view.printLine(out, "    Last Modified Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", tag.getLastModificationTime()));
                view.printLine(out, "    Last Accessed Date:", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", tag.getLastAccessTime()));
            }

            private void print(NtfsTimestampExtraFieldRecord.UnknownTag tag, View view, PrintStream out) {
                view.printLine(out, String.format("  (0x%04X) Unknown Tag:", tag.getSignature()), String.format("%d bytes", tag.getSize()));

                ByteArrayHexView.builder()
                                .data(tag.getData())
                                .offs(view.getOffs())
                                .columnWidth(view.getColumnWidth()).build().print(out);
            }
        });
    }

}
