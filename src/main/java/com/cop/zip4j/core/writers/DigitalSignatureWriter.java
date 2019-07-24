package com.cop.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import com.cop.zip4j.io.SplitOutputStream;
import com.cop.zip4j.model.CentralDirectory;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 13.04.2019
 */
@RequiredArgsConstructor
final class DigitalSignatureWriter {

    private final CentralDirectory.DigitalSignature digitalSignature;

    public void write(@NonNull SplitOutputStream out) throws IOException {
        if (digitalSignature == null)
            return;

        out.writeDword(CentralDirectory.DigitalSignature.SIGNATURE);
        out.writeWord(ArrayUtils.getLength(digitalSignature.getSignatureData()));
        out.writeBytes(digitalSignature.getSignatureData());
    }

}
