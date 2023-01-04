package ru.olegcherednik.zip4jvm.io.readers.block.zip64;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.zip64.EndCentralDirectoryReader;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;

/**
 * @author Oleg Cherednik
 * @since 29.12.2022
 */
@RequiredArgsConstructor
public class BlockEndCentralDirectoryReader extends EndCentralDirectoryReader {

    private final Block block;

    @Override
    public Zip64.EndCentralDirectory read(DataInput in) {
        return block.calcSize(in, () -> super.read(in));
    }

}
