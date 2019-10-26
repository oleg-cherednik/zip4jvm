package ru.olegcherednik.zip4jvm.view.extrafield;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.os.NtfsTimestampExtraField;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
@RequiredArgsConstructor
final class NtfsTimestampExtraFieldView {

    private final NtfsTimestampExtraField record;
    private final Block block;
    private final String prefix;

    public void print(PrintStream out) {
        out.format("%s(0x%04X) NTFS Timestamps:                       %d bytes\n", prefix, record.getSignature(), block.getSize());
        out.format("%s  - location:                                   %2$d (0x%2$08X) bytes\n", prefix, block.getOffs());
        out.format("%s  Creation Date:                                %2$tY-%2$tm-%2$td %2$tH:%2$tM:%2$tS\n", prefix, record.getCreationTime());
        out.format("%s  Last Modified Date:                           %2$tY-%2$tm-%2$td %2$tH:%2$tM:%2$tS\n", prefix,
                record.getLastModificationTime());
        out.format("%s  Last Accessed Date:                           %2$tY-%2$tm-%2$td %2$tH:%2$tM:%2$tS\n", prefix, record.getLastAccessTime());
    }

}
