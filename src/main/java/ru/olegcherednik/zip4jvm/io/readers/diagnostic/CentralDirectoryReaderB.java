package ru.olegcherednik.zip4jvm.io.readers.diagnostic;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.CentralDirectoryReader;
import ru.olegcherednik.zip4jvm.io.readers.DigitalSignatureReader;
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
public class CentralDirectoryReaderB extends CentralDirectoryReader {

    public CentralDirectoryReaderB(long totalEntries, Function<Charset, Charset> charsetCustomizer) {
        super(totalEntries, charsetCustomizer);
    }

    @Override
    public CentralDirectory read(DataInput in) throws IOException {
        return Block.foo(in, Diagnostic.getInstance().getCentralDirectory(), () -> super.read(in));
    }

    @Override
    protected FileHeaderReader getFileHeaderReader(long totalEntries, Function<Charset, Charset> charsetCustomizer) {
        return new FileHeaderReaderB(totalEntries, charsetCustomizer);
    }

    @Override
    protected DigitalSignatureReader getDigitalSignatureReader() {
        return new DigitalSignatureReaderB();
    }
}
