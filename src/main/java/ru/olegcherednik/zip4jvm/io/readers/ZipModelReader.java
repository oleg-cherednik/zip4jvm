package ru.olegcherednik.zip4jvm.io.readers;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.ZipInputStream;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 06.03.2019
 */
public final class ZipModelReader extends BaseZipModelReader {

    public ZipModelReader(SrcZip srcZip) {
        this(srcZip, Charsets.UNMODIFIED);
    }

    public ZipModelReader(SrcZip srcZip, Function<Charset, Charset> customizeCharset) {
        super(srcZip, customizeCharset);
    }

    public ZipModel read() throws IOException {
        readCentralData();
        return new ZipModelBuilder(srcZip, endCentralDirectory, zip64, centralDirectory, customizeCharset).build();
    }

    public static int getTotalDisks(SrcZip srcZip) {
        ZipModelReader reader = new ZipModelReader(srcZip);

        try (DataInput in = reader.createDataInput()) {
            reader.readEndCentralDirectory(in);
            reader.readZip64EndCentralDirectoryLocator(in);

            if (reader.zip64 == Zip64.NULL)
                return reader.endCentralDirectory.getTotalDisks() + 1;
            return (int)reader.zip64.getEndCentralDirectoryLocator().getTotalDisks();
        } catch(Exception e) {
            return 0;
        }
    }

    @Override
    protected DataInput createDataInput() throws IOException {
        return new ZipInputStream(srcZip);
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
