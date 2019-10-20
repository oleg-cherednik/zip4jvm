package ru.olegcherednik.zip4jvm.io.readers.block;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.CentralDirectoryReader;
import ru.olegcherednik.zip4jvm.io.readers.DigitalSignatureReader;
import ru.olegcherednik.zip4jvm.io.readers.FileHeaderReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
public class BlockCentralDirectoryReader extends CentralDirectoryReader {

    public BlockCentralDirectoryReader(long totalEntries, Function<Charset, Charset> charsetCustomizer) {
        super(totalEntries, charsetCustomizer);
    }

    @Override
    public CentralDirectory read(DataInput in) throws IOException {
        return Diagnostic.getInstance().getCentralDirectory().calc(in, () -> super.read(in));
    }

    @Override
    protected FileHeaderReader getFileHeaderReader() {
        return new BlockFileHeaderReader(totalEntries, charsetCustomizer);
    }

    @Override
    protected DigitalSignatureReader getDigitalSignatureReader() {
        return new BlockDigitalSignatureReader();
    }
}
