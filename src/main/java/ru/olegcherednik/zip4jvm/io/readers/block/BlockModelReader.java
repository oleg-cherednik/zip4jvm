package ru.olegcherednik.zip4jvm.io.readers.block;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.BaseZipModelReader;
import ru.olegcherednik.zip4jvm.io.readers.CentralDirectoryReader;
import ru.olegcherednik.zip4jvm.io.readers.EndCentralDirectoryReader;
import ru.olegcherednik.zip4jvm.model.Zip64;
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

    private final Diagnostic diagnostic;

    public BlockModelReader(Path zip, Function<Charset, Charset> charsetCustomizer, Diagnostic diagnostic) {
        super(zip, charsetCustomizer);
        this.diagnostic = diagnostic;
    }

    public BlockModel read() throws IOException {
        readData();
        return BlockModel.builder()
                         .zipModel(new ZipModelBuilder(zip, endCentralDirectory, zip64, centralDirectory, charsetCustomizer).build())
                         .diagnostic(diagnostic)
                         .endCentralDirectory(endCentralDirectory)
                         .zip64(zip64)
                         .centralDirectory(centralDirectory).build();
    }

    @Override
    protected EndCentralDirectoryReader getEndCentralDirectoryReader() {
        return new BlockEndCentralDirectoryReader(charsetCustomizer, diagnostic.getEndCentralDirectory());
    }

    @Override
    protected Zip64 readZip64(DataInput in) throws IOException {
        return new BlockZip64Reader(diagnostic.getZip64()).read(in);
    }

    @Override
    protected CentralDirectoryReader getCentralDirectoryReader(long totalEntries) {
        return new BlockCentralDirectoryReader(totalEntries, charsetCustomizer, diagnostic.getCentralDirectory());
    }
}

