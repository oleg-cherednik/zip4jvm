package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.OutputStreamDecorator;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.utils.InternalZipConstants;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 13.04.2019
 */
@RequiredArgsConstructor
final class DigitalSignatureWriter {

    private final CentralDirectory.DigitalSignature digitalSignature;

    public void write(@NonNull OutputStreamDecorator out) throws IOException {
        if (digitalSignature == null)
            return;

        out.writeDword(InternalZipConstants.DIGSIG);
        out.writeShort((short)ArrayUtils.getLength(digitalSignature.getSignatureData()));
        out.writeBytes(digitalSignature.getSignatureData());
    }

}
