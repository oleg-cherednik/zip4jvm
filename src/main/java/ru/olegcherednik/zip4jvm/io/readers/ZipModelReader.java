package ru.olegcherednik.zip4jvm.io.readers;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.io.in.file.Zip;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 06.03.2019
 */
public final class ZipModelReader extends BaseZipModelReader {

    public ZipModelReader(Path zip, Function<Charset, Charset> customizeCharset) {
        super(Zip.of(zip), customizeCharset);
    }

    public ZipModel read() throws IOException {
        readCentralData();
        return new ZipModelBuilder(zip, endCentralDirectory, zip64, centralDirectory, customizeCharset).build();
    }

    @Override
    protected DataInput createDataInput() throws IOException {
        return new SingleZipInputStream(zip);
    }

    @Override
    protected EndCentralDirectoryReader getEndCentralDirectoryReader() {
        return new EndCentralDirectoryReader(customizeCharset);
    }

    @Override
    protected Zip64Reader getZip64Reader() {
        return new Zip64Reader();
    }

    @Override
    protected CentralDirectoryReader getCentralDirectoryReader(long totalEntries) {
        return new CentralDirectoryReader(totalEntries, customizeCharset);
    }

}
