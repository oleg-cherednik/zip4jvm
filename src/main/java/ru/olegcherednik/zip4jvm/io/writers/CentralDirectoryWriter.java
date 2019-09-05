package ru.olegcherednik.zip4jvm.io.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 10.03.2019
 */
@RequiredArgsConstructor
public final class CentralDirectoryWriter {

    @NonNull
    private final CentralDirectory centralDirectory;

    public void write(@NonNull DataOutput out) throws IOException {
        // TODO check that exactly required byte were written
        new FileHeaderWriter(centralDirectory.getFileHeaders()).write(out);
        new DigitalSignatureWriter(centralDirectory.getDigitalSignature()).write(out);
    }

}
