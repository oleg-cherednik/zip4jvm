package ru.olegcherednik.zip4jvm.io.readers.diagnostic;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 05.03.2019
 */
@RequiredArgsConstructor
public class CentralDirectoryReaderA implements Reader<CentralDirectory> {

    private final long totalEntries;
    private final Function<Charset, Charset> charsetCustomizer;

    @Override
    public CentralDirectory read(DataInput in) throws IOException {
        return readCentralDirectory(in);
    }

    protected CentralDirectory readCentralDirectory(DataInput in) throws IOException {
        CentralDirectory centralDirectory = new CentralDirectory();
        centralDirectory.setFileHeaders(getFileHeaderReader(totalEntries, charsetCustomizer).read(in));
        centralDirectory.setDigitalSignature(getDigitalSignatureReader().read(in));
        return centralDirectory;
    }

    protected FileHeaderReaderA getFileHeaderReader(long totalEntries, Function<Charset, Charset> charsetCustomizer) {
        return new FileHeaderReaderA(totalEntries, charsetCustomizer);
    }

    protected DigitalSignatureReaderA getDigitalSignatureReader() {
        return new DigitalSignatureReaderA();
    }
}
