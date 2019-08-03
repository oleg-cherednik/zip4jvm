package com.cop.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import com.cop.zip4j.io.in.LittleEndianReadFile;
import com.cop.zip4j.model.CentralDirectory;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 13.04.2019
 */
@RequiredArgsConstructor
final class DigitalSignatureReader {

    @NonNull
    public CentralDirectory.DigitalSignature read(@NonNull LittleEndianReadFile in) throws IOException {
        if (in.readDword() != CentralDirectory.DigitalSignature.SIGNATURE)
            return null;

        CentralDirectory.DigitalSignature digitalSignature = new CentralDirectory.DigitalSignature();
        digitalSignature.setSignatureData(in.readBytes(in.readWord()));
        return digitalSignature;
    }
}
