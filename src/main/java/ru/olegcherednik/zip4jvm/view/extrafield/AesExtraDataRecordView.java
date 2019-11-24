package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.AesExtraDataRecord;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.CompressionMethodView;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class AesExtraDataRecordView extends View implements IExtraFieldView {

    private final AesExtraDataRecord record;
    private final GeneralPurposeFlag generalPurposeFlag;
    private final Block block;

    public static Builder builder() {
        return new Builder();
    }

    private AesExtraDataRecordView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        record = builder.record;
        generalPurposeFlag = builder.generalPurposeFlag;
        block = builder.block;
    }

    @Override
    public boolean print(PrintStream out) {
        printValueLocation(out, String.format("(0x%04X) AES Encryption Tag:", record.getSignature()), block);
        printLine(out, "  Encryption Tag Version:", String.format("%s-%d", record.getVendor(), record.getVersionNumber()));
        printLine(out, "  Encryption Key Bits:", record.getStrength().getSize());

        CompressionMethodView.builder()
                             .compressionMethod(record.getCompressionMethod())
                             .generalPurposeFlag(generalPurposeFlag)
                             .offs(offs + 2)
                             .columnWidth(columnWidth).build().print(out);

        return true;
    }

    @Override
    public int getSignature() {
        return record.getSignature();
    }

    @Override
    public String getTitle() {
        return "AES Encryption Tag";
    }

    @Override
    public String getFileName() {
        return String.format("(0x%04X)_AES_Encryption_Tag", record.getSignature());
    }

    public static final class Builder {

        private AesExtraDataRecord record;
        private GeneralPurposeFlag generalPurposeFlag;
        private Block block;
        private int offs;
        private int columnWidth;

        public AesExtraDataRecordView build() {
            Objects.requireNonNull(record, "'record' must not be null");
            Objects.requireNonNull(block, "'block' must not be null");
            return new AesExtraDataRecordView(this);
        }

        public Builder record(AesExtraDataRecord record) {
            this.record = record == AesExtraDataRecord.NULL ? null : record;
            return this;
        }

        public Builder generalPurposeFlag(GeneralPurposeFlag generalPurposeFlag) {
            this.generalPurposeFlag = generalPurposeFlag;
            return this;
        }

        public Builder block(Block block) {
            this.block = block == Block.NULL ? null : block;
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
