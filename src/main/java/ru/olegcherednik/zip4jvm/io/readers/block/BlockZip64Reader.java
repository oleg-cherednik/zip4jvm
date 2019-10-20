package ru.olegcherednik.zip4jvm.io.readers.block;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.Zip64Reader;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
public final class BlockZip64Reader extends Zip64Reader {

    @Override
    protected Zip64.EndCentralDirectoryLocator readEndCentralDirectoryLocator(DataInput in) throws IOException {
        Diagnostic.getInstance().addZip64();
        Block block = Diagnostic.getInstance().getZip64().getEndCentralDirectoryLocator();
        return block.calc(in, () -> super.readEndCentralDirectoryLocator(in));
    }

    @Override
    protected Zip64.EndCentralDirectory readEndCentralDirectory(DataInput in) throws IOException {
        Block block = Diagnostic.getInstance().getZip64().getEndCentralDirectory();
        return block.calc(in, () -> super.readEndCentralDirectory(in));
    }

}
