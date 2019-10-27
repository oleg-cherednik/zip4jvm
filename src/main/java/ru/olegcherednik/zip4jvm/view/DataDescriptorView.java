package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.block.Block;

import java.io.PrintStream;
import java.util.stream.IntStream;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
@Builder
public class DataDescriptorView {

    private final DataDescriptor dataDescriptor;
    private final Block block;
    private final long pos;
    private final String prefix;

    public void print(PrintStream out) {
        if (dataDescriptor == null)
            return;

        String str = String.format("#%d (%s) Data descriptor", pos + 1, ViewUtils.signature(DataDescriptor.SIGNATURE));
        out.println(str);

        IntStream.range(0, str.length()).forEach(i -> out.print('='));

        out.println();
        out.format("%s  - location:                                   %2$d (0x%2$08X) bytes\n", prefix, block.getOffs());
        out.format("%s  - size:                                       %d bytes\n", prefix, block.getSize());
        out.format("%s32-bit CRC value:                               0x%2$08X\n", prefix, dataDescriptor.getCrc32());
        out.format("%scompressed size:                                %d bytes\n", prefix, dataDescriptor.getCompressedSize());
        out.format("%suncompressed size:                              %d bytes\n", prefix, dataDescriptor.getUncompressedSize());
    }

}
