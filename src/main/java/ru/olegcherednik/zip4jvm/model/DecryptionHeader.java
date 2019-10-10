package ru.olegcherednik.zip4jvm.model;

import lombok.Getter;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;

/**
 * @author Oleg Cherednik
 * @since 11.10.2019
 */
@Getter
@Setter
public class DecryptionHeader {

    // size:2 - size of initialization vector (n)
    private int ivSize;
    // size:n - initialization vector for this file
    private byte[] iv;
    // size:4 - size of remaining decryption header data
    private long size;
    // size:2 - format definition for this record
    private int format;
    // size:2 - encryption algorithm identifier
    private EncryptionAlgorithm encryptionAlgorithm;
    // size:2 - bit length of encryption key
    private int bitLength;
    // size:2 - Processing flags
    private int flags;
    // size:2 - size of Encrypted Random Data (m)
    private int encryptedRandomDataSize;
    // size:m - encrypted random data
    private byte[] encryptedRandomData;
    // size:2 - size of initialization vector (k)
    private int passwordValidationDataSize;
    // size:k - password validation data
    private byte[] passwordValidationData;
    // size:4 - checksum of password validation data
    private long crc32;

}
