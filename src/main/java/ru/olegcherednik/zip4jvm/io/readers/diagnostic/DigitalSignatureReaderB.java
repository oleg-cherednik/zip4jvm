package ru.olegcherednik.zip4jvm.io.readers.diagnostic;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.diagnostic.Block;
import ru.olegcherednik.zip4jvm.model.diagnostic.Diagnostic;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
@RequiredArgsConstructor
public class DigitalSignatureReaderB extends DigitalSignatureReaderA {

    @Override
    protected CentralDirectory.DigitalSignature readDigitalSignature(DataInput in) throws IOException {
        Diagnostic.CentralDirectory centralDirectory = Diagnostic.getInstance().getCentralDirectory();
        centralDirectory.addDigitalSignature();

        return Block.foo(in, centralDirectory.getDigitalSignature(), () -> super.readDigitalSignature(in));
    }
}
