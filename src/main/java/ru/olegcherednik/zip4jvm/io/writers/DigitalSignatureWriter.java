package ru.olegcherednik.zip4jvm.io.writers;

import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 13.04.2019
 */
@RequiredArgsConstructor
final class DigitalSignatureWriter {

    private final CentralDirectory.DigitalSignature digitalSignature;

    public void write(@NonNull DataOutput out) throws IOException {
        if (digitalSignature == null)
            return;

        out.writeDwordSignature(CentralDirectory.DigitalSignature.SIGNATURE);
        out.writeWord(ArrayUtils.getLength(digitalSignature.getSignatureData()));
        out.writeBytes(digitalSignature.getSignatureData());
    }

}
