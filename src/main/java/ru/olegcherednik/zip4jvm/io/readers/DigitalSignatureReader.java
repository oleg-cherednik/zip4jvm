package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;

import static ru.olegcherednik.zip4jvm.io.readers.ZipModelReader.MARK_DIGITAL_SIGNATURE_END_OFFS;
import static ru.olegcherednik.zip4jvm.io.readers.ZipModelReader.MARK_DIGITAL_SIGNATURE_OFFS;

/**
 * @author Oleg Cherednik
 * @since 13.04.2019
 */
@RequiredArgsConstructor
final class DigitalSignatureReader implements Reader<CentralDirectory.DigitalSignature> {

    @Override
    public CentralDirectory.DigitalSignature read(DataInput in) throws IOException {
        in.mark(MARK_DIGITAL_SIGNATURE_OFFS);

        if (in.readSignature() != CentralDirectory.DigitalSignature.SIGNATURE)
            return null;

        CentralDirectory.DigitalSignature digitalSignature = new CentralDirectory.DigitalSignature();
        digitalSignature.setSignatureData(in.readBytes(in.readWord()));

        in.mark(MARK_DIGITAL_SIGNATURE_END_OFFS);

        return digitalSignature;
    }
}
