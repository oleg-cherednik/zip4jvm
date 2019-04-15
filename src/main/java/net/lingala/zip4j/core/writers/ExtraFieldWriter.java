package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.OutputStreamDecorator;
import net.lingala.zip4j.model.ExtraField;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@RequiredArgsConstructor
final class ExtraFieldWriter {

    private final ExtraField extraField;
    @NonNull
    private final Charset charset;

    public void write(@NonNull OutputStreamDecorator out) throws IOException {
        if (extraField != null) {
            new Zip64ExtendedInfoWriter(extraField.getZip64ExtendedInfo()).write(out);
            new AESExtraDataRecordWriter(extraField.getAesExtraDataRecord(), charset).write(out);
        }
    }

}
