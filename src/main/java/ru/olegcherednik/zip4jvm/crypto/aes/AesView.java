package ru.olegcherednik.zip4jvm.crypto.aes;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

import static ru.olegcherednik.zip4jvm.crypto.aes.AesEngine.MAC_SIZE;
import static ru.olegcherednik.zip4jvm.crypto.aes.AesEngine.PASSWORD_CHECKSUM_SIZE;

/**
 * @author Oleg Cherednik
 * @since 23.10.2019
 */
@Getter
public class AesView implements Decoder {

    private final byte[] salt;
    private final byte[] passwordChecksum;
    private byte[] mac;

    public AesView(ZipEntry zipEntry, DataInput in) throws IOException {
        salt = in.readBytes(zipEntry.getStrength().saltLength());
        passwordChecksum = in.readBytes(PASSWORD_CHECKSUM_SIZE);
    }

    @Override
    public void decrypt(byte[] buf, int offs, int len) {
    }

    @Override
    public long getCompressedSize(ZipEntry zipEntry) {
        return zipEntry.getCompressedSize() - salt.length - PASSWORD_CHECKSUM_SIZE - MAC_SIZE;
    }

    @Override
    public void close(DataInput in) throws IOException {
        mac = in.readBytes(MAC_SIZE);
    }
}
