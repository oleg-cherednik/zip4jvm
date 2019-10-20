package ru.olegcherednik.zip4jvm.io.readers.block;

import ru.olegcherednik.zip4jvm.io.readers.BaseZipModelReader;
import ru.olegcherednik.zip4jvm.io.readers.CentralDirectoryReader;
import ru.olegcherednik.zip4jvm.io.readers.EndCentralDirectoryReader;
import ru.olegcherednik.zip4jvm.io.readers.Zip64Reader;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 19.10.2019
 */
public final class BlockModelReader extends BaseZipModelReader {

    public BlockModelReader(Path zip, Function<Charset, Charset> charsetCustomizer) {
        super(zip, charsetCustomizer);
    }

    public BlockModel read() throws IOException {
        Diagnostic.createInstance();
        readData();
        return BlockModel.builder()
                         .diagnostic(Diagnostic.removeInstance())
                         .endCentralDirectory(endCentralDirectory)
                         .zip64(zip64)
                         .centralDirectory(centralDirectory).build();
    }

    @Override
    protected EndCentralDirectoryReader getEndCentralDirectoryReader() {
        return new BlockEndCentralDirectoryReader(charsetCustomizer);
    }

    @Override
    protected Zip64Reader getZip64Reader() {
        return new BlockZip64Reader();
    }

    @Override
    protected CentralDirectoryReader getCentralDirectoryReader(long totalEntries) {
        return new BlockCentralDirectoryReader(totalEntries, charsetCustomizer);
    }
}

