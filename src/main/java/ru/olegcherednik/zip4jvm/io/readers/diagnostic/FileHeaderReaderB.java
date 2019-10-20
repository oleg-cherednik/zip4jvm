package ru.olegcherednik.zip4jvm.io.readers.diagnostic;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.ExtraFieldReader;
import ru.olegcherednik.zip4jvm.io.readers.FileHeaderReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.diagnostic.Block;
import ru.olegcherednik.zip4jvm.model.diagnostic.Diagnostic;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
public class FileHeaderReaderB extends FileHeaderReader {

    public FileHeaderReaderB(long totalEntries, Function<Charset, Charset> charsetCustomizer) {
        super(totalEntries, charsetCustomizer);
    }

    @Override
    protected CentralDirectory.FileHeader readFileHeader(DataInput in) throws IOException {
        Diagnostic.CentralDirectory centralDirectory = Diagnostic.getInstance().getCentralDirectory();
        centralDirectory.createFileHeader();

        CentralDirectory.FileHeader fileHeader = Block.foo(in, centralDirectory.getFileHeader(), () -> super.readFileHeader(in));
        centralDirectory.saveFileHeader(fileHeader.getFileName());

        return fileHeader;
    }

    @Override
    protected ExtraFieldReader getExtraFiledReader(int size, CentralDirectory.FileHeader fileHeader) {
        return new ExtraFieldReaderB(size, ExtraFieldReader.getReaders(fileHeader));
    }

}
