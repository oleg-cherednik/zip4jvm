package ru.olegcherednik.zip4jvm.io.readers.block;

import ru.olegcherednik.zip4jvm.io.readers.BaseZipModelReader;
import ru.olegcherednik.zip4jvm.io.readers.CentralDirectoryReader;
import ru.olegcherednik.zip4jvm.io.readers.EndCentralDirectoryReader;
import ru.olegcherednik.zip4jvm.io.readers.Zip64Reader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 19.10.2019
 */
public final class BlockModelReader extends BaseZipModelReader {

    private final Diagnostic diagnostic = new Diagnostic();

    public BlockModelReader(Path zip, Function<Charset, Charset> customizeCharset) {
        super(zip, customizeCharset);
    }

    public BlockModel read() throws IOException {
        readData();

        ZipModel zipModel = new ZipModelBuilder(zip, endCentralDirectory, zip64, centralDirectory, customizeCharset).build();

        return BlockModel.builder()
                         .zipModel(zipModel)
                         .diagnostic(diagnostic)
                         .endCentralDirectory(endCentralDirectory)
                         .zip64(zip64)
                         .centralDirectory(centralDirectory).build();
    }

    @Override
    protected EndCentralDirectoryReader getEndCentralDirectoryReader() {
        return new BlockEndCentralDirectoryReader(customizeCharset, diagnostic.getEndCentralDirectoryBlock());
    }

    @Override
    protected Zip64Reader getZip64Reader() {
        return new BlockZip64Reader(diagnostic.getZip64Block());
    }

    @Override
    protected CentralDirectoryReader getCentralDirectoryReader(long totalEntries) {
        return new BlockCentralDirectoryReader(totalEntries, customizeCharset, diagnostic.getCentralDirectoryBlock());
    }
}

