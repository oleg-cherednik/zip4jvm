package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 05.03.2019
 */
@RequiredArgsConstructor
final class CentralDirectoryReader implements Reader<CentralDirectory> {

    private final long offs;
    private final long totalEntries;

    @Override
    public CentralDirectory read(DataInput in) throws IOException {
        findHead(in);

        CentralDirectory centralDirectory = new CentralDirectory();
        centralDirectory.setFileHeaders(new FileHeaderReader(totalEntries).read(in));
        centralDirectory.setDigitalSignature(new DigitalSignatureReader().read(in));
        return centralDirectory;
    }

    private void findHead(DataInput in) throws IOException {
        in.seek(offs);
    }
}
