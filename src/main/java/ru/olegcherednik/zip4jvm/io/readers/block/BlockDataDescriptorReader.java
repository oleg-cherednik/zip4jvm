package ru.olegcherednik.zip4jvm.io.readers.block;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.DataDescriptorReader;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
public class BlockDataDescriptorReader implements Reader<DataDescriptor> {

    private final DataDescriptorReader reader;
    private final Block block;

    public BlockDataDescriptorReader(boolean zip64, Block block) {
        reader = DataDescriptorReader.get(zip64);
        this.block = block;
    }

    @Override
    public DataDescriptor read(DataInput in) throws IOException {
        return block.calc(in, () -> reader.read(in));
    }

}
