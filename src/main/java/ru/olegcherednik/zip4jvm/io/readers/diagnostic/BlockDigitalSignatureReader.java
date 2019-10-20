package ru.olegcherednik.zip4jvm.io.readers.diagnostic;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.DigitalSignatureReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.diagnostic.Diagnostic;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
@RequiredArgsConstructor
public class BlockDigitalSignatureReader extends DigitalSignatureReader {

    @Override
    protected CentralDirectory.DigitalSignature readDigitalSignature(DataInput in) throws IOException {
        Diagnostic.CentralDirectory centralDirectory = Diagnostic.getInstance().getCentralDirectory();
        centralDirectory.addDigitalSignature();
        return centralDirectory.getDigitalSignature().wrapper(in, () -> super.readDigitalSignature(in));
    }
}
