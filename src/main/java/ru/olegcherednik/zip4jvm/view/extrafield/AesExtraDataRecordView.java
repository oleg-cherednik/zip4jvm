package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.AesExtraDataRecord;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.CompressionMethodView;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class AesExtraDataRecordView extends View {

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
    public void print(PrintStream out) {
        if (record.isNull())
            return;

        printLine(out, String.format("(0x%04X) AES Encryption Tag:", record.getSignature()), String.format("%d bytes", block.getSize()));
        printLine(out, "  - location:", String.format("%1$d (0x%1$08X) bytes", block.getOffs()));
        printLine(out, "  Encryption Tag Version:", String.format("%s-%d", record.getVendor(), record.getVersionNumber()));
        printLine(out, "  Encryption Key Bits:", String.format("%s", record.getStrength().getSize()));

        CompressionMethodView.builder()
                             .compressionMethod(record.getCompressionMethod())
                             .generalPurposeFlag(generalPurposeFlag)
                             .offs(offs + 2)
                             .columnWidth(columnWidth).build().print(out);
    }

    public static final class Builder {

        private AesExtraDataRecord record = AesExtraDataRecord.NULL;
        private GeneralPurposeFlag generalPurposeFlag;
        private Block block;
        private int offs;
        private int columnWidth;

        public AesExtraDataRecordView build() {
            return new AesExtraDataRecordView(this);
        }

        public Builder record(AesExtraDataRecord record) {
            this.record = Optional.ofNullable(record).orElse(AesExtraDataRecord.NULL);
            return this;
        }

        public Builder generalPurposeFlag(GeneralPurposeFlag generalPurposeFlag) {
            this.generalPurposeFlag = generalPurposeFlag;
            return this;
        }

        public Builder block(Block block) {
            this.block = block;
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
