package ru.olegcherednik.zip4jvm.view.extrafield;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.os.InfoZipNewUnixExtraField;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
@RequiredArgsConstructor
final class InfoZipNewUnixExtraFieldView {

    private final InfoZipNewUnixExtraField record;
    private final Block block;
    private final String prefix;

    public void print(PrintStream out) {
        out.format("%s(0x%04X) new InfoZIP Unix/OS2/NT:               %d bytes\n", prefix, record.getSignature(), block.getSize());
        out.format("%s  - location:                                   %2$d (0x%2$08X) bytes\n", prefix, block.getOffs());

        if (record.getVersion() == 1) {
            out.format("%s  version:                                      %d\n", prefix, record.getVersion());

            InfoZipNewUnixExtraField.VersionOnePayload payload = record.getPayload();

            if (StringUtils.isNotBlank(payload.getUid()))
                out.format("%s  User identifier (UID):                        %s\n", prefix, payload.getUid());
            if (StringUtils.isNotBlank(payload.getGid()))
                out.format("%s  Group Identifier (GID):                       %s\n", prefix, payload.getGid());
        } else {
            out.format("%s  version:                                      %d (unknown)\n", prefix, record.getVersion());

            ByteArrayHexView.builder()
                            .buf(((InfoZipNewUnixExtraField.VersionUnknownPayload)record.getPayload()).getData())
                            .prefix(prefix).build().print(out);
        }
    }
}
