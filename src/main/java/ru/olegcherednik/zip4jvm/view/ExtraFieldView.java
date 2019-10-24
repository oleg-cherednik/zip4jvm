package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.model.AesExtraDataRecord;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.NtfsTimestampExtraField;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 16.10.2019
 */
@Builder
public class ExtraFieldView {

    private final ExtraField extraField;
    private final Diagnostic.ExtraField diagExtraField;
    private final GeneralPurposeFlag generalPurposeFlag;
    private final String prefix;

    public void print(PrintStream out) {
        if (extraField.getSize() == 0)
            return;

        out.format("%sextra field location:                           %2$d (0x%2$08X) bytes\n", prefix, diagExtraField.getOffs());
        out.format("%s  - size:                                       %d bytes (%d records)\n",
                prefix, diagExtraField.getSize(), extraField.getRecords().size());

        for (ExtraField.Record record : extraField.getRecords()) {
            if (record.isNull())
                continue;
            if (record instanceof NtfsTimestampExtraField)
                print((NtfsTimestampExtraField)record, out);
            else if (record instanceof Zip64.ExtendedInfo)
                print((Zip64.ExtendedInfo)record, out);
            else if (record instanceof AesExtraDataRecord)
                print((AesExtraDataRecord)record, out);
            else if (record instanceof ExtraField.Record.Unknown)
                print((ExtraField.Record.Unknown)record, out);
            else
                throw new Zip4jvmException(String.format("View for ExtraField record (0x%04X) is not implemented", record.getSignature()));
        }
    }

    private void print(NtfsTimestampExtraField record, PrintStream out) {
        Block block = diagExtraField.getRecord(record.getSignature());

        out.format("%s(0x%04X) NTFS Timestamps:                       %d bytes\n", prefix, record.getSignature(), block.getSize());
        out.format("%s  - location:                                   %2$d (0x%2$08X) bytes\n", prefix, block.getOffs());
        out.format("%s  Creation Date:                                %2$tY-%2$tm-%2$td %2$tH:%2$tM:%2$tS\n", prefix, record.getCreationTime());
        out.format("%s  Last Modified Date:                           %2$tY-%2$tm-%2$td %2$tH:%2$tM:%2$tS\n", prefix,
                record.getLastModificationTime());
        out.format("%s  Last Accessed Date:                           %2$tY-%2$tm-%2$td %2$tH:%2$tM:%2$tS\n", prefix, record.getLastAccessTime());
    }

    private void print(Zip64.ExtendedInfo record, PrintStream out) {
        Block block = diagExtraField.getRecord(record.getSignature());

        out.format("%s(0x%04X) Zip64 Extended Information:            %d bytes\n", prefix, record.getSignature(), block.getSize());
        out.format("%s  - location:                                   %2$d (0x%2$08X) bytes\n", prefix, block.getOffs());

        if (record.getUncompressedSize() != ExtraField.NO_DATA)
            out.format("%s  original compressed size                      %d bytes\n", prefix, record.getUncompressedSize());
        if (record.getCompressedSize() != ExtraField.NO_DATA)
            out.format("%s  original uncompressed size:                   %d bytes\n", prefix, record.getCompressedSize());
        if (record.getLocalFileHeaderOffs() != ExtraField.NO_DATA)
            out.format("%s  original relative offset of local header:     %2$d (0x%2$08X) bytes\n", prefix, record.getLocalFileHeaderOffs());
        if (record.getDisk() != ExtraField.NO_DATA)
            out.format("%s  original part number of this part (%04X):     %d\n", prefix, record.getDisk(), record.getDisk());
    }

    private void print(AesExtraDataRecord record, PrintStream out) {
        Block block = diagExtraField.getRecord(record.getSignature());
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

    private void print(ExtraField.Record.Unknown record, PrintStream out) {
        Block block = diagExtraField.getRecord(record.getSignature());

        out.format("%s(0x%04X) Unknown:                               %d bytes; \n", prefix, record.getSignature(), block.getSize());
        out.format("%s  - location:                                   %2$d (0x%2$08X) bytes\n", prefix, block.getOffs());

        ByteArrayHexView.builder()
                        .buf(record.getBlockData())
                        .prefix(prefix).build().print(out);
    }

}
