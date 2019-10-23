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

    @Override
    public AesEncryptionHeader read(DataInput in) throws IOException {
        AesEncryptionHeader encryptionHeader = new AesEncryptionHeader();
        encryptionHeader.getSalt().calc(in, () -> in.readBytes(strength.saltLength()));
        encryptionHeader.getPasswordChecksum().calc(in, () -> in.readBytes(PASSWORD_CHECKSUM_SIZE));
        in.skip(AesEngine.getDataCompressedSize(compressedSize, strength.saltLength()));
        encryptionHeader.getMac().calc(in, () -> in.readBytes(MAC_SIZE));
        return encryptionHeader;
    }

}
