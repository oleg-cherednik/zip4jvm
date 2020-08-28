package ru.olegcherednik.zip4jvm.io.readers.block;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.ZipInputStream;
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

    private final Block block;

    public BlockEndCentralDirectoryReader(Function<Charset, Charset> customizeCharset, Block block) {
        super(customizeCharset);
        this.block = block;
    }

    @Override
    public EndCentralDirectory read(DataInput in) throws IOException {
        // TODO temporary
        block.setOffs(in.getOffs() - in.getSrcFile().getDisks().get((int)in.getDisk()).getAbsOffs());
        EndCentralDirectory endCentralDirectory = super.read(in);

        if (in instanceof ZipInputStream) {
//            ((ZipInputStream)in).setDisk(endCentralDirectory.getMainDisk());
            ((ZipInputStream)in).setFileName(in.getFileName());
        }

        block.setDisk(in.getDisk(), in.getFileName());
        block.setSrcZip(in.getSrcFile());
        block.calc(in.getOffs() - in.getSrcFile().getDisks().get((int)in.getDisk()).getAbsOffs());

        return endCentralDirectory;
    }

}
