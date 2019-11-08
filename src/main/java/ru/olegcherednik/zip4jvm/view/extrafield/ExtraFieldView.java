package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.AesExtraDataRecord;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;
import ru.olegcherednik.zip4jvm.model.os.ExtendedTimestampExtraField;
import ru.olegcherednik.zip4jvm.model.os.InfoZipNewUnixExtraField;
import ru.olegcherednik.zip4jvm.model.os.InfoZipOldUnixExtraField;
import ru.olegcherednik.zip4jvm.model.os.NtfsTimestampExtraField;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 16.10.2019
 */
public final class ExtraFieldView extends View {

    private final ExtraField extraField;
    private final Diagnostic.ExtraField diagExtraField;
    private final GeneralPurposeFlag generalPurposeFlag;

    public static Builder builder() {
        return new Builder();
    }

    private ExtraFieldView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        extraField = builder.extraField;
        diagExtraField = builder.diagExtraField;
        generalPurposeFlag = builder.generalPurposeFlag;
    }

    @Override
    public void print(PrintStream out) {
        int total = extraField.getTotalRecords();

        if (total == 0)
            return;

        printLocation(out);
        printSize(total, out);
        printRecords(out);
    }

    private void printLocation(PrintStream out) {
        printLine(out, "extra field location:", String.format("%1$d (0x%1$08X) bytes", diagExtraField.getOffs()));
    }

    private void printSize(int total, PrintStream out) {
        if (total == 1)
            printLine(out, "  - size:", String.format("%d bytes (1 record)", diagExtraField.getSize()));
        else
            printLine(out, "  - size:", String.format("%d bytes (%d records)", diagExtraField.getSize(), total));
    }

    private void printRecords(PrintStream out) {
        for (ExtraField.Record record : extraField.getRecords()) {
            if (record.isNull())
                continue;

            Block block = diagExtraField.getRecord(record.getSignature());

            if (record instanceof NtfsTimestampExtraField)
                NtfsTimestampExtraFieldView.builder()
                                           .record((NtfsTimestampExtraField)record)
                                           .block(block)
                                           .offs(offs)
                                           .columnWidth(columnWidth).build().print(out);
            else if (record instanceof InfoZipOldUnixExtraField)
                InfoZipOldUnixExtraFieldView.builder()
                                            .record((InfoZipOldUnixExtraField)record)
                                            .block(block)
                                            .offs(offs)
                                            .columnWidth(columnWidth).build().print(out);
            else if (record instanceof InfoZipNewUnixExtraField)
                InfoZipNewUnixExtraFieldView.builder()
                                            .record((InfoZipNewUnixExtraField)record)
                                            .block(block)
                                            .offs(offs)
                                            .columnWidth(columnWidth).build().print(out);
            else if (record instanceof ExtendedTimestampExtraField)
                ExtendedTimestampExtraFieldView.builder()
                                               .record((ExtendedTimestampExtraField)record)
                                               .block(block)
                                               .offs(offs)
                                               .columnWidth(columnWidth).build().print(out);
            else if (record instanceof Zip64.ExtendedInfo)
                Zip64ExtendedInfoView.builder()
                                     .record((Zip64.ExtendedInfo)record)
                                     .block(block)
                                     .offs(offs)
                                     .columnWidth(columnWidth).build().print(out);
            else if (record instanceof AesExtraDataRecord)
                AesExtraDataRecordView.builder()
                                      .record((AesExtraDataRecord)record)
                                      .generalPurposeFlag(generalPurposeFlag)
                                      .block(block)
                                      .offs(offs)
                                      .columnWidth(columnWidth).build().print(out);
            else if (record instanceof ExtraField.Record.Unknown)
                UnknownView.builder()
                           .record((ExtraField.Record.Unknown)record)
                           .block(block)
                           .offs(offs)
                           .columnWidth(columnWidth).build().print(out);
        }
    }

    public static final class Builder {

        private ExtraField extraField;
        private Diagnostic.ExtraField diagExtraField;
        private GeneralPurposeFlag generalPurposeFlag;
        private int offs;
        private int columnWidth;

        public ExtraFieldView build() {
            return new ExtraFieldView(this);
        }

        public Builder extraField(ExtraField extraField) {
            this.extraField = extraField;
            return this;
        }

        public Builder diagExtraField(Diagnostic.ExtraField diagExtraField) {
            this.diagExtraField = diagExtraField;
            return this;
        }

        public Builder generalPurposeFlag(GeneralPurposeFlag generalPurposeFlag) {
            this.generalPurposeFlag = generalPurposeFlag;
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
