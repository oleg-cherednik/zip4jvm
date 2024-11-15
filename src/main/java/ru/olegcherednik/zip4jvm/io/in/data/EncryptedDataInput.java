package ru.olegcherednik.zip4jvm.io.in.data;

import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

/**
 * @author Oleg Cherednik
 * @since 15.11.2024
 */
public class EncryptedDataInput extends BaseDataInput {

    private final Decoder decoder;

    public static EncryptedDataInput create(ZipEntry zipEntry, DataInput in) {
        return new EncryptedDataInput(zipEntry.createDecoder(in), in);
    }

    protected EncryptedDataInput(Decoder decoder, DataInput in) {
        super(in);
        this.decoder = decoder;
    }

}
