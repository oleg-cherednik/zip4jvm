package ru.olegcherednik.zip4jvm.io.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 13.04.2019
 */
@RequiredArgsConstructor
final class DigitalSignatureReader implements Reader<CentralDirectory.DigitalSignature> {

    @NonNull
    @Override
    public CentralDirectory.DigitalSignature read(@NonNull DataInput in) throws IOException {
        if (in.readSignature() != CentralDirectory.DigitalSignature.SIGNATURE)
            return null;

        CentralDirectory.DigitalSignature digitalSignature = new CentralDirectory.DigitalSignature();
        digitalSignature.setSignatureData(in.readBytes(in.readWord()));
        return digitalSignature;
    }
}
