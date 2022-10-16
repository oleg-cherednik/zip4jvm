package ru.olegcherednik.zip4jvm.crypto.strong;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

/**
 * see 7.2.4
 *
 * @author Oleg Cherednik
 * @since 11.10.2019
 */
@Getter
@Setter
public class DecryptionHeader {

    // TODO if 0 - then CRC32 + 64bit FileSize should be used to decrypt data
    // size:2 - size of initialization vector (n)
    // size:n - initialization vector for this file
    private byte[] iv;
    // size:4 - size of remaining decryption header data
    // size:2 - format definition for this record (should be 3)
    private int format;
    // size:2 - encryption algorithm identifier
    private int encryptionAlgorithmCode;
    private EncryptionAlgorithm encryptionAlgorithm;
    // size:2 - bit length of encryption key
    private int bitLength;
    // size:2 - Processing flags
    private Flags flags;
    // size:2 - size of Encrypted Random Data (m)
    // size:m - encrypted random data
    private byte[] encryptedRandomData;
    // size:4 - number of recipients (n)
    // size:2 - hash algorithm to be used to calculate Public Key hash (absent for password based encryption)
    private int hashAlgorithmCode;
    private HashAlgorithm hashAlgorithm;
    // size:2 - size of Public Key hash (absent for password based encryption) (p)
    // size:n - Recipient List Element (absent for password based encryption)
    private List<Recipient> recipients = Collections.emptyList();
    // size:2 - size of password validation data (k)
    // size:k - password validation data
    private byte[] passwordValidationData;
    // size:4 - checksum of password validation data
    private long crc32;

    public void setEncryptionAlgorithm(int code) {
        encryptionAlgorithmCode = code;
        encryptionAlgorithm = EncryptionAlgorithm.parseCode(code);
    }

    public void setHashAlgorithm(int code) {
        hashAlgorithmCode = code;
        hashAlgorithm = HashAlgorithm.parseCode(code);
    }

}
