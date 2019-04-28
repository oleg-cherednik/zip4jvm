package net.lingala.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.LittleEndianRandomAccessFile;
import net.lingala.zip4j.model.CentralDirectory;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 13.04.2019
 */
@RequiredArgsConstructor
final class DigitalSignatureReader {

    @NonNull
    public CentralDirectory.DigitalSignature read(@NonNull LittleEndianRandomAccessFile in) throws IOException {
        if (in.readInt() != CentralDirectory.DigitalSignature.SIGNATURE)
            return null;

        CentralDirectory.DigitalSignature digitalSignature = new CentralDirectory.DigitalSignature();
        digitalSignature.setSignatureData(in.readBytes(in.readWord()));
        return digitalSignature;
    }
}
