package ru.olegcherednik.zip4jvm.io.readers;

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
public class CentralDirectoryReader implements Reader<CentralDirectory> {

    private final long totalEntries;
    private final Function<Charset, Charset> charsetCustomizer;

    @Override
    public CentralDirectory read(DataInput in) throws IOException {
        CentralDirectory centralDirectory = new CentralDirectory();
        centralDirectory.setFileHeaders(getFileHeaderReader(totalEntries, charsetCustomizer).read(in));
        centralDirectory.setDigitalSignature(getDigitalSignatureReader().read(in));
        return centralDirectory;
    }

    protected FileHeaderReader getFileHeaderReader(long totalEntries, Function<Charset, Charset> charsetCustomizer) {
        return new FileHeaderReader(totalEntries, charsetCustomizer);
    }

    protected DigitalSignatureReader getDigitalSignatureReader() {
        return new DigitalSignatureReader();
    }
}
