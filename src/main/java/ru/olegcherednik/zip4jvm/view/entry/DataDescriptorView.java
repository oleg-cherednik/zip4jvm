package ru.olegcherednik.zip4jvm.view.entry;

import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
public final class DataDescriptorView extends View {

    private final DataDescriptor dataDescriptor;
    private final Block block;
    private final long pos;

    public DataDescriptorView(DataDescriptor dataDescriptor, Block block, long pos, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.dataDescriptor = dataDescriptor;
        this.block = block;
        this.pos = pos;

        Objects.requireNonNull(dataDescriptor, "'dataDescriptor' must not be null");
        Objects.requireNonNull(block, "'block' must not be null");
    }

    @Override
    public boolean print(PrintStream out) {
        printSubTitle(out, DataDescriptor.SIGNATURE, pos, "Data descriptor", block);
        printLine(out, "32-bit CRC value:", String.format("0x%08X", dataDescriptor.getCrc32()));
        printLine(out, "compressed size:", String.format("%d bytes", dataDescriptor.getCompressedSize()));
        printLine(out, "uncompressed size:", String.format("%d bytes", dataDescriptor.getUncompressedSize()));
        return true;
    }

}
