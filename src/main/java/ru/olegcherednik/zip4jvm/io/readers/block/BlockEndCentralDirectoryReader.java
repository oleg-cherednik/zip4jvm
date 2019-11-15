package ru.olegcherednik.zip4jvm.io.readers.block;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.EndCentralDirectoryReader;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.Block;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
public class BlockEndCentralDirectoryReader extends EndCentralDirectoryReader {

    private final Block endCentralDirectory;

    public BlockEndCentralDirectoryReader(Function<Charset, Charset> charsetCustomizer, Block endCentralDirectory) {
        super(charsetCustomizer);
        this.endCentralDirectory = endCentralDirectory;
    }

    @Override
    public EndCentralDirectory read(DataInput in) throws IOException {
        EndCentralDirectory res = endCentralDirectory.calc(in, () -> super.read(in));
        return res;
    }

}
