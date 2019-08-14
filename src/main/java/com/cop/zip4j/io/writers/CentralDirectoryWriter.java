package com.cop.zip4j.io.writers;

import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.CentralDirectory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 10.03.2019
 */
@RequiredArgsConstructor
public final class CentralDirectoryWriter {

    @NonNull
    private final CentralDirectory dir;
    @NonNull
    private final Charset charset;

    public void write(@NonNull DataOutput out) throws IOException {
        new FileHeaderWriter(dir.getFileHeaders(), charset).write(out);
        new DigitalSignatureWriter(dir.getDigitalSignature()).write(out);
    }

}
