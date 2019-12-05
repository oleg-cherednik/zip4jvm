package ru.olegcherednik.zip4jvm.view.extrafield;

import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.model.AesExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.os.ExtendedTimestampExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.os.InfoZipNewUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.os.InfoZipOldUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.os.NtfsTimestampExtraFieldRecord;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 16.10.2019
 */
public final class ExtraFieldView extends View {

    private final ExtraField extraField;
    private final ExtraFieldBlock block;
    private final GeneralPurposeFlag generalPurposeFlag;
    private final Function<Block, byte[]> getDataFunc;

    public static Builder builder() {
        return new Builder();
    }

    private ExtraFieldView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        extraField = builder.extraField;
        block = builder.block;
        generalPurposeFlag = builder.generalPurposeFlag;
        getDataFunc = builder.getDataFunc;
    }

    @Override
    public boolean print(PrintStream out) {
        printLocation(out);
        printSize(extraField.getTotalRecords(), out);
        printRecords(out);
        return true;
    }

    private void printLocation(PrintStream out) {
        printLine(out, "extra field location:", String.format("%1$d (0x%1$08X) bytes", block.getOffs()));
    }

    private void printSize(int total, PrintStream out) {
        if (total == 1)
            printLine(out, "  - size:", String.format("%d bytes (1 record)", block.getSize()));
        else
            printLine(out, "  - size:", String.format("%d bytes (%d records)", block.getSize(), total));
    }

    private void printRecords(PrintStream out) {
        extraField.getRecords().forEach(record -> printRecord(out, record));
    }

    public void printRecord(PrintStream out, ExtraField.Record record) {
        if (record != null && !record.isNull())
            getView(record).print(out);
    }

    public ExtraFieldRecordView<?> getView(ExtraField.Record record) {
        // TODO check for record != null && !record.isNull()
        return createView.apply(record);
    }

    private final Function<ExtraField.Record, ExtraFieldRecordView<?>> createView = record -> {
        if (record instanceof NtfsTimestampExtraFieldRecord)
            return createView((NtfsTimestampExtraFieldRecord)record);
        if (record instanceof InfoZipOldUnixExtraFieldRecord)
            return createView((InfoZipOldUnixExtraFieldRecord)record);
        if (record instanceof InfoZipNewUnixExtraFieldRecord)
            return createView((InfoZipNewUnixExtraFieldRecord)record);
        if (record instanceof ExtendedTimestampExtraFieldRecord)
            return createView((ExtendedTimestampExtraFieldRecord)record);
        if (record instanceof Zip64.ExtendedInfo)
            return createView((Zip64.ExtendedInfo)record);
        if (record instanceof AesExtraFieldRecord)
            return createView((AesExtraFieldRecord)record);
        return createView(record);
    };

    private NtfsTimestampExtraFieldRecordView createView(NtfsTimestampExtraFieldRecord record) {
        return NtfsTimestampExtraFieldRecordView.builder()
                                                .record(record)
                                                .block(block.getRecordBlock(record.getSignature()))
                                                .position(offs, columnWidth).build();
    }

    private InfoZipOldUnixExtraFieldRecordView createView(InfoZipOldUnixExtraFieldRecord record) {
        return InfoZipOldUnixExtraFieldRecordView.builder()
                                                 .record(record)
                                                 .block(block.getRecordBlock(record.getSignature()))
                                                 .position(offs, columnWidth).build();
    }

    private InfoZipNewUnixExtraFieldRecordView createView(InfoZipNewUnixExtraFieldRecord record) {
        return InfoZipNewUnixExtraFieldRecordView.builder()
                                                 .record(record)
                                                 .block(block.getRecordBlock(record.getSignature()))
                                                 .position(offs, columnWidth).build();
    }

    private ExtendedTimestampExtraFieldRecordView createView(ExtendedTimestampExtraFieldRecord record) {
        return ExtendedTimestampExtraFieldRecordView.builder()
                                                    .record(record)
                                                    .block(block.getRecordBlock(record.getSignature()))
                                                    .position(offs, columnWidth).build();
    }

    private Zip64ExtendedInfoView createView(Zip64.ExtendedInfo record) {
        return Zip64ExtendedInfoView.builder()
                                    .record(record)
                                    .block(block.getRecordBlock(record.getSignature()))
                                    .position(offs, columnWidth).build();
    }

    private AesExtraFieldRecordView createView(AesExtraFieldRecord record) {
        return AesExtraFieldRecordView.builder()
                                      .record(record)
                                      .generalPurposeFlag(generalPurposeFlag)
                                      .block(block.getRecordBlock(record.getSignature()))
                                      .position(offs, columnWidth).build();
    }

    private UnknownExtraFieldRecordView createView(ExtraField.Record record) {
        Block recordBlock = block.getRecordBlock(record.getSignature());
        return UnknownExtraFieldRecordView.builder()
                                          .record(record)
                                          .block(recordBlock)
                                          .data(getDataFunc.apply(recordBlock))
                                          .position(offs, columnWidth).build();
    }

    public static final class Builder {

        private ExtraField extraField;
        private ExtraFieldBlock block;
        private GeneralPurposeFlag generalPurposeFlag;
        private Function<Block, byte[]> getDataFunc = block -> ArrayUtils.EMPTY_BYTE_ARRAY;
        private int offs;
        private int columnWidth;

        public ExtraFieldView build() {
            Objects.requireNonNull(extraField, "'extraField' must not be null");
            Objects.requireNonNull(block, "'block' must not be null");
            Objects.requireNonNull(generalPurposeFlag, "'generalPurposeFlag' must not be null");
            Objects.requireNonNull(getDataFunc, "'getDataFunc' must not be null");
            return new ExtraFieldView(this);
        }

        public Builder extraField(ExtraField extraField) {
            this.extraField = extraField == ExtraField.NULL || extraField.getTotalRecords() == 0 ? null : extraField;
            return this;
        }

        public Builder block(ExtraFieldBlock block) {
            this.block = block;
            return this;
        }

        public Builder generalPurposeFlag(GeneralPurposeFlag generalPurposeFlag) {
            this.generalPurposeFlag = generalPurposeFlag;
            return this;
        }

        public Builder getDataFunc(Function<Block, byte[]> getDataFunc) {
            this.getDataFunc = Optional.ofNullable(getDataFunc).orElseGet(() -> block -> ArrayUtils.EMPTY_BYTE_ARRAY);
            return this;
        }

        public Builder position(int offs, int columnWidth) {
            this.offs = offs;
            this.columnWidth = columnWidth;
            return this;
        }
    }

}
