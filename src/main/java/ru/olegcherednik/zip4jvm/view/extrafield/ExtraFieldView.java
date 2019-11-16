package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.AesExtraDataRecord;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.os.ExtendedTimestampExtraField;
import ru.olegcherednik.zip4jvm.model.os.InfoZipNewUnixExtraField;
import ru.olegcherednik.zip4jvm.model.os.InfoZipOldUnixExtraField;
import ru.olegcherednik.zip4jvm.model.os.NtfsTimestampExtraField;
import ru.olegcherednik.zip4jvm.view.IView;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 16.10.2019
 */
public final class ExtraFieldView extends View {

    private final ExtraField extraField;
    private final ExtraFieldBlock extraFieldBlock;
    private final GeneralPurposeFlag generalPurposeFlag;

    public static Builder builder() {
        return new Builder();
    }

    private ExtraFieldView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        extraField = builder.extraField;
        extraFieldBlock = builder.extraFieldBlock;
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
        printLine(out, "extra field location:", String.format("%1$d (0x%1$08X) bytes", extraFieldBlock.getOffs()));
    }

    private void printSize(int total, PrintStream out) {
        if (total == 1)
            printLine(out, "  - size:", String.format("%d bytes (1 record)", extraFieldBlock.getSize()));
        else
            printLine(out, "  - size:", String.format("%d bytes (%d records)", extraFieldBlock.getSize(), total));
    }

    private void printRecords(PrintStream out) {
        extraField.getRecords().forEach(record -> printRecord(out, record));
    }

    public void printRecord(PrintStream out, ExtraField.Record record) {
        if (record != null && !record.isNull())
            createView.apply(record).print(out);
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
                                          .block(extraFieldBlock.getRecord(record.getSignature()))
                                          .offs(offs)
                                          .columnWidth(columnWidth).build();
    }

    private IView createView(InfoZipOldUnixExtraField record) {
        return InfoZipOldUnixExtraFieldView.builder()
                                           .record(record)
                                           .block(extraFieldBlock.getRecord(record.getSignature()))
                                           .offs(offs)
                                           .columnWidth(columnWidth).build();
    }

    private IView createView(InfoZipNewUnixExtraField record) {
        return InfoZipNewUnixExtraFieldView.builder()
                                           .record(record)
                                           .block(extraFieldBlock.getRecord(record.getSignature()))
                                           .offs(offs)
                                           .columnWidth(columnWidth).build();
    }

    private IView createView(ExtendedTimestampExtraField record) {
        return ExtendedTimestampExtraFieldView.builder()
                                              .record(record)
                                              .block(extraFieldBlock.getRecord(record.getSignature()))
                                              .offs(offs)
                                              .columnWidth(columnWidth).build();
    }

    private IView createView(Zip64.ExtendedInfo record) {
        return Zip64ExtendedInfoView.builder()
                                    .record(record)
                                    .block(extraFieldBlock.getRecord(record.getSignature()))
                                    .offs(offs)
                                    .columnWidth(columnWidth).build();
    }

    private IView createView(AesExtraDataRecord record) {
        return AesExtraDataRecordView.builder()
                                     .record(record)
                                     .generalPurposeFlag(generalPurposeFlag)
                                     .block(extraFieldBlock.getRecord(record.getSignature()))
                                     .offs(offs)
                                     .columnWidth(columnWidth).build();
    }

    private IView createView(ExtraField.Record.Unknown record) {
        return UnknownView.builder()
                          .record(record)
                          .block(extraFieldBlock.getRecord(record.getSignature()))
                          .offs(offs)
                          .columnWidth(columnWidth).build();
    }

    public static final class Builder {

        private ExtraField extraField;
        private ExtraFieldBlock extraFieldBlock;
        private GeneralPurposeFlag generalPurposeFlag;
        private int offs;
        private int columnWidth;

        public ExtraFieldView build() {
            Objects.requireNonNull(extraField, "'extraField' must not be null");
            Objects.requireNonNull(extraFieldBlock, "'extraFieldBlock' must not be null");
            Objects.requireNonNull(generalPurposeFlag, "'generalPurposeFlag' must not be null");
            return new ExtraFieldView(this);
        }

        public Builder extraField(ExtraField extraField) {
            this.extraField = extraField == ExtraField.NULL || extraField.getTotalRecords() == 0 ? null : extraField;
            return this;
        }

        public Builder extraFieldBlock(ExtraFieldBlock extraFieldBlock) {
            this.extraFieldBlock = extraFieldBlock;
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
