package ru.olegcherednik.zip4jvm.view.extrafield;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
@RequiredArgsConstructor
final class UnknownView {

    private final ExtraField.Record.Unknown record;
    private final Block block;
    private final String prefix;

    public void print(PrintStream out) {
        out.format("%s(0x%04X) Unknown:                               %d bytes; \n", prefix, record.getSignature(), block.getSize());
        out.format("%s  - location:                                   %2$d (0x%2$08X) bytes\n", prefix, block.getOffs());

        ByteArrayHexView.builder()
                        .buf(record.getBlockData())
                        .prefix(prefix).build().print(out);
    }
}
