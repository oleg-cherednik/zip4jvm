package ru.olegcherednik.zip4jvm.view.extrafield;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.os.InfoZipOldUnixExtraField;

import java.io.PrintStream;

import static ru.olegcherednik.zip4jvm.model.ExtraField.NO_DATA;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
@RequiredArgsConstructor
final class InfoZipOldUnixExtraFieldView {

    private final InfoZipOldUnixExtraField record;
    private final Block block;
    private final String prefix;

    public void print(PrintStream out) {
        out.format("%s(0x%04X) old InfoZIP Unix/OS2/NT:               %d bytes\n", prefix, record.getSignature(), block.getSize());
        out.format("%s  - location:                                   %2$d (0x%2$08X) bytes\n", prefix, block.getOffs());
        out.format("%s  Last Modified Date:                           %2$tY-%2$tm-%2$td %2$tH:%2$tM:%2$tS\n", prefix,
                record.getLastModificationTime());
        out.format("%s  Last Accessed Date:                           %2$tY-%2$tm-%2$td %2$tH:%2$tM:%2$tS\n", prefix, record.getLastAccessTime());

        if (record.getUid() != NO_DATA)
            out.format("%s  User identifier (UID):                        %d\n", prefix, record.getUid());
        if (record.getGid() != NO_DATA)
            out.format("%s  Group Identifier (GID):                       %d\n", prefix, record.getGid());
    }
}
