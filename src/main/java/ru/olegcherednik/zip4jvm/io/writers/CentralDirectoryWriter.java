package ru.olegcherednik.zip4jvm.io.writers;

import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
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
    private final CentralDirectory centralDirectory;
    @NonNull
    private final Charset charset;

    public void write(@NonNull DataOutput out) throws IOException {
        // TODO check that exactly required byte were written
        new FileHeaderWriter(centralDirectory.getFileHeaders(), charset).write(out);
        new DigitalSignatureWriter(centralDirectory.getDigitalSignature()).write(out);
    }

}
