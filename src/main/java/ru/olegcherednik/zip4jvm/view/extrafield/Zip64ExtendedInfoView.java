package ru.olegcherednik.zip4jvm.view.extrafield;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
@RequiredArgsConstructor
final class Zip64ExtendedInfoView {

    private final Zip64.ExtendedInfo record;
    private final Block block;
    private final String prefix;

    public void print(PrintStream out) {
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
}
