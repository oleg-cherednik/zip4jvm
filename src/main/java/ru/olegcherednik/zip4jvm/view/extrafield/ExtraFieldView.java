package ru.olegcherednik.zip4jvm.view.extrafield;

import lombok.Builder;
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

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 16.10.2019
 */
@Builder
public final class ExtraFieldView {

    private final ExtraField extraField;
    private final Diagnostic.ExtraField diagExtraField;
    private final GeneralPurposeFlag generalPurposeFlag;
    private final String prefix;

    public void print(PrintStream out) {
        int total = extraField.getTotalRecords();

        if (total == 0)
            return;

        printLocation(out);
        printSize(total, out);
        printRecords(out);
    }

    private void printLocation(PrintStream out) {
        out.format("%sextra field location:                           %2$d (0x%2$08X) bytes\n", prefix, diagExtraField.getOffs());
    }

    private void printSize(int total, PrintStream out) {
        if (total == 1)
            out.format("%s  - size:                                       %d bytes (1 record)\n", prefix, diagExtraField.getSize());
        else
            out.format("%s  - size:                                       %d bytes (%d records)\n", prefix, diagExtraField.getSize(), total);
    }

    private void printRecords(PrintStream out) {
        for (ExtraField.Record record : extraField.getRecords()) {
            if (record.isNull())
                continue;

            Block block = diagExtraField.getRecord(record.getSignature());

            if (record instanceof NtfsTimestampExtraField)
                new NtfsTimestampExtraFieldView((NtfsTimestampExtraField)record, block, prefix).print(out);
            else if (record instanceof InfoZipOldUnixExtraField)
                new InfoZipOldUnixExtraFieldView((InfoZipOldUnixExtraField)record, block, prefix).print(out);
            else if (record instanceof InfoZipNewUnixExtraField)
                new InfoZipNewUnixExtraFieldView((InfoZipNewUnixExtraField)record, block, prefix).print(out);
            else if (record instanceof ExtendedTimestampExtraField)
                new ExtendedTimestampExtraFieldView((ExtendedTimestampExtraField)record, block, prefix).print(out);
            else if (record instanceof Zip64.ExtendedInfo)
                new Zip64ExtendedInfoView((Zip64.ExtendedInfo)record, block, prefix).print(out);
            else if (record instanceof AesExtraDataRecord)
                new AesExtraDataRecordView((AesExtraDataRecord)record, generalPurposeFlag, block, prefix).print(out);
            else if (record instanceof ExtraField.Record.Unknown)
                new UnknownView((ExtraField.Record.Unknown)record, block, prefix).print(out);
        }
    }

}
