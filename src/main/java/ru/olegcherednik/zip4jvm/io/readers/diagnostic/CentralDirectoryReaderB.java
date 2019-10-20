package ru.olegcherednik.zip4jvm.io.readers.diagnostic;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
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
public class CentralDirectoryReaderB extends CentralDirectoryReaderA {

    public CentralDirectoryReaderB(long totalEntries, Function<Charset, Charset> charsetCustomizer) {
        super(totalEntries, charsetCustomizer);
    }

    @Override
    protected CentralDirectory readCentralDirectory(DataInput in) throws IOException {
        return Block.foo(in, Diagnostic.getInstance().getCentralDirectory(), () -> super.readCentralDirectory(in));
    }

    @Override
    protected FileHeaderReaderA getFileHeaderReader(long totalEntries, Function<Charset, Charset> charsetCustomizer) {
        return new FileHeaderReaderB(totalEntries, charsetCustomizer);
    }

    @Override
    protected DigitalSignatureReaderA getDigitalSignatureReader() {
        return new DigitalSignatureReaderB();
    }
}
