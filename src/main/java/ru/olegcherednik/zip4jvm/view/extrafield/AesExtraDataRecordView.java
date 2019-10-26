package ru.olegcherednik.zip4jvm.view.extrafield;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.AesExtraDataRecord;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.CompressionMethodView;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
@RequiredArgsConstructor
final class AesExtraDataRecordView {

    private final AesExtraDataRecord record;
    private final GeneralPurposeFlag generalPurposeFlag;
    private final Block block;
    private final String prefix;

    public void print(PrintStream out) {
        CompressionMethod compressionMethod = record.getCompressionMethod();

        out.format("%s(0x%04X) AES Encryption Tag:                    %d bytes\n", prefix, record.getSignature(), block.getSize());
        out.format("%s  - location:                                   %2$d (0x%2$08X) bytes\n", prefix, block.getOffs());
        out.format("%s  Encryption Tag Version:                       %s-%d\n", prefix, record.getVendor(), record.getVersionNumber());
        out.format("%s  Encryption Key Bits:                          %s\n", prefix, record.getStrength().getSize());

        CompressionMethodView.builder()
                             .compressionMethod(compressionMethod)
                             .generalPurposeFlag(generalPurposeFlag)
                             .prefix(prefix + "  ").build().print(out);
    }
}
