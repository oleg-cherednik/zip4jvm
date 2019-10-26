package ru.olegcherednik.zip4jvm.view.extrafield;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.os.ExtendedTimestampExtraField;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
@RequiredArgsConstructor
final class ExtendedTimestampExtraFieldView {

    private final ExtendedTimestampExtraField record;
    private final Block block;
    private final String prefix;

    public void print(PrintStream out) {
        out.format("%s(0x%04X) Universal time:                        %d bytes\n", prefix, record.getSignature(), block.getSize());
        out.format("%s  - location:                                   %2$d (0x%2$08X) bytes\n", prefix, block.getOffs());

        if (record.getFlag().isLastModificationTime())
            out.format("%s  Last Modified Date:                           %2$tY-%2$tm-%2$td %2$tH:%2$tM:%2$tS\n", prefix,
                    record.getLastModificationTime());

        if (record.getFlag().isLastAccessTime())
            out.format("%s  Last Accessed Date:                           %2$tY-%2$tm-%2$td %2$tH:%2$tM:%2$tS\n", prefix, record.getLastAccessTime());

        if (record.getFlag().isCreationTime())
            out.format("%s  Creation Date:                                %2$tY-%2$tm-%2$td %2$tH:%2$tM:%2$tS\n", prefix, record.getCreationTime());
    }
}
