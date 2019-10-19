package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Diagnostic;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 13.04.2019
 */
@RequiredArgsConstructor
final class DigitalSignatureReader implements Reader<CentralDirectory.DigitalSignature> {

    @Override
    public CentralDirectory.DigitalSignature read(DataInput in) throws IOException {
        long offs = in.getOffs();

        if (in.readSignature() != CentralDirectory.DigitalSignature.SIGNATURE)
            return null;

        Diagnostic.getInstance().getCentralDirectory().setDigitalSignatureOffs(offs);

        CentralDirectory.DigitalSignature digitalSignature = new CentralDirectory.DigitalSignature();
        digitalSignature.setSignatureData(in.readBytes(in.readWord()));

        Diagnostic.getInstance().getCentralDirectory().setDigitalSignatureEndOffs(in.getOffs());

        return digitalSignature;
    }
}
