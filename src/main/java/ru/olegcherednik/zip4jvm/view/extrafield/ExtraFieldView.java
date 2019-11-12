package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.AesExtraDataRecord;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;
import ru.olegcherednik.zip4jvm.model.os.ExtendedTimestampExtraField;
import ru.olegcherednik.zip4jvm.model.os.InfoZipNewUnixExtraField;
import ru.olegcherednik.zip4jvm.model.os.InfoZipOldUnixExtraField;
import ru.olegcherednik.zip4jvm.model.os.NtfsTimestampExtraField;
import ru.olegcherednik.zip4jvm.view.IView;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Optional;
import java.util.function.Function;

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
    public boolean print(PrintStream out) {
        printLocation(out);
        printSize(extraField.getTotalRecords(), out);
        printRecords(out);
        return true;
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
        extraField.getRecords().stream()
                  .filter(record -> !record.isNull())
                  .map(createView)
                  .forEach(view -> view.print(out));
    }

    private final Function<ExtraField.Record, IView> createView = record -> {
        if (record instanceof NtfsTimestampExtraField)
            return createView((NtfsTimestampExtraField)record);
        if (record instanceof InfoZipOldUnixExtraField)
            return createView((InfoZipOldUnixExtraField)record);
        if (record instanceof InfoZipNewUnixExtraField)
            return createView((InfoZipNewUnixExtraField)record);
        if (record instanceof ExtendedTimestampExtraField)
            return createView((ExtendedTimestampExtraField)record);
        if (record instanceof Zip64.ExtendedInfo)
            return createView((Zip64.ExtendedInfo)record);
        if (record instanceof AesExtraDataRecord)
            return createView((AesExtraDataRecord)record);
        return createView((ExtraField.Record.Unknown)record);
    };

    private IView createView(NtfsTimestampExtraField record) {
        return NtfsTimestampExtraFieldView.builder()
                                          .record(record)
                                          .block(diagExtraField.getRecord(record.getSignature()))
                                          .offs(offs)
                                          .columnWidth(columnWidth).build();
    }

    private IView createView(InfoZipOldUnixExtraField record) {
        return InfoZipOldUnixExtraFieldView.builder()
                                           .record(record)
                                           .block(diagExtraField.getRecord(record.getSignature()))
                                           .offs(offs)
                                           .columnWidth(columnWidth).build();
    }

    private IView createView(InfoZipNewUnixExtraField record) {
        return InfoZipNewUnixExtraFieldView.builder()
                                           .record(record)
                                           .block(diagExtraField.getRecord(record.getSignature()))
                                           .offs(offs)
                                           .columnWidth(columnWidth).build();
    }

    private IView createView(ExtendedTimestampExtraField record) {
        return ExtendedTimestampExtraFieldView.builder()
                                              .record(record)
                                              .block(diagExtraField.getRecord(record.getSignature()))
                                              .offs(offs)
                                              .columnWidth(columnWidth).build();
    }

    private IView createView(Zip64.ExtendedInfo record) {
        return Zip64ExtendedInfoView.builder()
                                    .record(record)
                                    .block(diagExtraField.getRecord(record.getSignature()))
                                    .offs(offs)
                                    .columnWidth(columnWidth).build();
    }

    private IView createView(AesExtraDataRecord record) {
        return AesExtraDataRecordView.builder()
                                     .record(record)
                                     .generalPurposeFlag(generalPurposeFlag)
                                     .block(diagExtraField.getRecord(record.getSignature()))
                                     .offs(offs)
                                     .columnWidth(columnWidth).build();
    }

    private IView createView(ExtraField.Record.Unknown record) {
        return UnknownView.builder()
                          .record(record)
                          .block(diagExtraField.getRecord(record.getSignature()))
                          .offs(offs)
                          .columnWidth(columnWidth).build();
    }

    public static final class Builder {

        private ExtraField extraField = ExtraField.NULL;
        private Diagnostic.ExtraField diagExtraField;
        private GeneralPurposeFlag generalPurposeFlag;
        private int offs;
        private int columnWidth;

        public IView build() {
            return extraField.getTotalRecords() == 0 || diagExtraField == null ? IView.NULL : new ExtraFieldView(this);
        }

        public Builder extraField(ExtraField extraField) {
            this.extraField = Optional.ofNullable(extraField).orElse(ExtraField.NULL);
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
