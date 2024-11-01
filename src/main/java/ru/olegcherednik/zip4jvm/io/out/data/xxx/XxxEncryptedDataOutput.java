package ru.olegcherednik.zip4jvm.io.out.data.xxx;

import ru.olegcherednik.zip4jvm.crypto.Encoder;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 01.11.2024
 */
public class XxxEncryptedDataOutput extends XxxBaseDataOutput {

    private final Encoder encoder;

    public static XxxEncryptedDataOutput create(ZipEntry zipEntry, DataOutput out) {
        return new XxxEncryptedDataOutput(zipEntry.createEncoder(), out);
    }

    protected XxxEncryptedDataOutput(Encoder encoder, DataOutput out) {
        super(out);
        this.encoder = encoder;
    }

    public void writeEncryptionHeader() throws IOException {
        encoder.writeEncryptionHeader(delegate);
    }

    public void encodingAccomplished() throws IOException {
        encoder.close(delegate);
    }

}
