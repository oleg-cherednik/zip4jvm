package net.lingala.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 13.04.2019
 */
@RequiredArgsConstructor
final class DigitalSignatureReader {

    @NonNull
    public CentralDirectory.DigitalSignature read(@NonNull LittleEndianRandomAccessFile in) throws IOException {
        if (in.readInt() != InternalZipConstants.DIGSIG)
            return null;

        CentralDirectory.DigitalSignature digitalSignature = new CentralDirectory.DigitalSignature();

        short size = in.readShort();
        digitalSignature.setSignatureData(in.readBytes(size));

        return digitalSignature;
    }
}
