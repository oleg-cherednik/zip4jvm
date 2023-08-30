package ru.olegcherednik.zip4jvm.io.readers.cd;

import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.crypto.aes.AesDecoder;
import ru.olegcherednik.zip4jvm.crypto.aes.AesEngine;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeaderDecoder;
import ru.olegcherednik.zip4jvm.io.in.buf.SimpleDataInputLocation;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputLocation;
import ru.olegcherednik.zip4jvm.io.in.file.DataInputFile;
import ru.olegcherednik.zip4jvm.io.readers.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import javax.crypto.Cipher;
import javax.crypto.Mac;

/**
 * @author Oleg Cherednik
 * @since 30.08.2023
 */
public class AesCentralDirectoryDecoder implements Decoder {

    public static AesCentralDirectoryDecoder create(DataInput in, char[] password, DecryptionHeaderReader decryptionHeaderReader, long compressedSize) {
        return Quietly.doQuietly(() -> {
            DecryptionHeader decryptionHeader = decryptionHeaderReader.read(in);
            Cipher cipher = new DecryptionHeaderDecoder(password).readAndCreateCipher(in.getEndianness(), decryptionHeader);
            DataInputLocation dataInputLocation = new SimpleDataInputLocation((DataInputFile)in);

            long decryptionHeaderSize = in.getMarkSize(EncryptedCentralDirectoryReader.DECRYPTION_HEADER);
            long compressedSize1 = extensibleDataSector.getCompressedSize() - decryptionHeaderSize;

            byte[] encrypted = getEncryptedByteArrayReader(compressedSize1).read(in);
            byte[] decrypted = decrypt(encrypted, cipher);
            byte[] decompressed = decompressData(decrypted, in.getEndianness(), dataInputLocation);



            AesStrength strength = AesEngine.getStrength(zipEntry.getEncryptionMethod());
            byte[] salt = in.readBytes(strength.saltLength());
            byte[] key = AesEngine.createKey(zipEntry.getPassword(), salt, strength);

            Cipher cipher = AesEngine.createCipher(strength.createSecretKeyForCipher(key));
            byte[] passwordChecksum = strength.createPasswordChecksum(key);
            checkPasswordChecksum(passwordChecksum, zipEntry, in);

            Mac mac = AesEngine.createMac(strength.createSecretKeyForMac(key));
            AesEngine engine = new AesEngine(cipher, mac);
            long compressedSize = AesEngine.getDataCompressedSize(zipEntry.getCompressedSize(), strength);
            return new AesCentralDirectoryDecoder(engine, compressedSize);
        });
    }

    @Override
    public long getCompressedSize() {
        return 0;
    }

    @Override
    public int decrypt(byte[] buf, int offs, int len) {
        return 0;
    }
}
