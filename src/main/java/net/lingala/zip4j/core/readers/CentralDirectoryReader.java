package net.lingala.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;

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
    public CentralDirectory read(@NonNull LittleEndianRandomAccessFile in) throws IOException {
        findHead(in);

        CentralDirectory dir = new CentralDirectory();
        dir.setFileHeaders(new FileHeaderReader(totalEntries).read(in));
        dir.setDigitalSignature(new DigitalSignatureReader().read(in));

        return dir;
    }

    private void findHead(LittleEndianRandomAccessFile in) throws IOException {
        in.seek(offs);
    }
}
