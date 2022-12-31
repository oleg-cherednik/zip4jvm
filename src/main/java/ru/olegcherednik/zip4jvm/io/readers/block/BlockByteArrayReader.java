package ru.olegcherednik.zip4jvm.io.readers.block;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.ByteArrayReader;
import ru.olegcherednik.zip4jvm.model.block.Block;

/**
 * @author Oleg Cherednik
 * @since 30.12.2022
 */
public class BlockByteArrayReader extends ByteArrayReader {

    private final Block block;

    public BlockByteArrayReader(int size, Block block) {
        super(size);
        this.block = block;
    }

    @Override
    public byte[] read(DataInput in) {
        return block.calcSize(in, () -> super.read(in));
    }
}
