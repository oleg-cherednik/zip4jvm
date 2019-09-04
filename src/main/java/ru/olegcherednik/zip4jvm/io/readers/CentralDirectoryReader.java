package ru.olegcherednik.zip4jvm.io.readers;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 05.03.2019
 */
@RequiredArgsConstructor
final class CentralDirectoryReader {

    private final long offs;
    private final long totalEntries;

    @NonNull
    public CentralDirectory read(@NonNull DataInput in) throws IOException {
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
