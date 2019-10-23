package ru.olegcherednik.zip4jvm.io.readers.block.aes;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.crypto.aes.AesEngine;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;

import static ru.olegcherednik.zip4jvm.crypto.aes.AesEngine.MAC_SIZE;
import static ru.olegcherednik.zip4jvm.crypto.aes.AesEngine.PASSWORD_CHECKSUM_SIZE;

/**
 * @author Oleg Cherednik
 * @since 23.10.2019
 */
@RequiredArgsConstructor
public class BlockAesHeaderReader implements Reader<AesEncryptionHeader> {

    private final AesStrength strength;
    private final long compressedSize;

    private byte[] salt;
    private byte[] passwordChecksum;
    private byte[] mac;

    @Override
    public AesEncryptionHeader read(DataInput in) throws IOException {
        AesEncryptionHeader encryptionHeader = new AesEncryptionHeader();
        salt = encryptionHeader.getSalt().calc(in, () -> in.readBytes(strength.saltLength()));
        passwordChecksum = encryptionHeader.getPasswordChecksum().calc(in, () -> in.readBytes(PASSWORD_CHECKSUM_SIZE));
        // TODO should be fixed; skip is not working with split zip and with over int
        in.skip((int)AesEngine.getDataCompressedSize(compressedSize, strength.saltLength()));
        mac = encryptionHeader.getMac().calc(in, () -> in.readBytes(MAC_SIZE));

        int a = 0;
        a++;

        return encryptionHeader;
    }

}
