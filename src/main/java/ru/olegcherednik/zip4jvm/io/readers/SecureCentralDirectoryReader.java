package ru.olegcherednik.zip4jvm.io.readers;

import ru.olegcherednik.zip4jvm.crypto.aes.AesEngine;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrongDecoder;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.BaseDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.function.Function;
import java.util.zip.Inflater;

/**
 * @author Oleg Cherednik
 * @since 10.10.2019
 */
public final class SecureCentralDirectoryReader extends CentralDirectoryReader {

    private final Zip64.ExtensibleDataSector extensibleDataSector;

    public SecureCentralDirectoryReader(long totalEntries, Function<Charset, Charset> customizeCharset,
                                        Zip64.ExtensibleDataSector extensibleDataSector) {
        super(totalEntries, customizeCharset);
        this.extensibleDataSector = extensibleDataSector;

        // TODO require not NULL extensibleDataSector
    }

    @Override
    public CentralDirectory read(DataInput in) throws IOException {
        try {
            DecryptionHeader decryptionHeader = new DecryptionHeaderReader().read(in);
            AesStrength strength = AesEngine.getStrength(decryptionHeader.getEncryptionAlgorithm().getEncryptionMethod());
            Cipher cipher = AesStrongDecoder.createCipher(decryptionHeader, strength, "1".toCharArray());
            byte[] passwordValidationData = cipher.update(decryptionHeader.getPasswordValidationData());

            long actual = DecryptionHeader.getActualCrc32(passwordValidationData);
            long expected = DecryptionHeader.getExpectedCrc32(passwordValidationData);

            if (expected != actual)
                throw new IncorrectPasswordException("Central Directory");

            AesStrongDecoder decoder = new AesStrongDecoder(cipher, (int)extensibleDataSector.getCompressedSize());

            byte[] bufBuf = in.readBytes((int)extensibleDataSector.getCompressedSize());
            byte[] aaa = cipher.update(bufBuf);

            DataInput dataInput = new BaseDataInput() {

                private int offs;

                @Override
                public long skip(long bytes) throws IOException {
                    return 0;
                }

                @Override
                public void seek(long absoluteOffs) throws IOException {
                    int a = 0;
                    a++;
                }

                @Override
                public void close() throws IOException {
                    int a = 0;
                    a++;
                }

                @Override
                public long getAbsoluteOffs() {
                    return offs;
                }

                @Override
                public long convertToAbsoluteOffs(int diskNo, long relativeOffs) {
                    return 0;
                }

                @Override
                public long getDiskRelativeOffs() {
                    return offs;
                }

                @Override
                public SrcZip getSrcZip() {
                    return null;
                }

                @Override
                public SrcZip.Disk getDisk() {
                    return null;
                }

                @Override
                public long size() throws IOException {
                    return 0;
                }

                @Override
                public int read(byte[] buf, int offs, int len) throws IOException {
                    for (int i = 0; i < len; i++)
                        buf[offs + i] = aaa[this.offs++];
                    return len;
                }

                @Override
                public long toLong(byte[] buf, int offs, int len) {
                    long res = 0;

                    for (int i = offs + len - 1; i >= offs; i--)
                        res = res << 8 | buf[i] & 0xFF;

                    return res;
                }

                @Override
                public void seek(int diskNo, long relativeOffs) throws IOException {

                }
            };

            CentralDirectoryReader centralDirectoryReader = new CentralDirectoryReader(totalEntries, customizeCharset);
            CentralDirectory centralDirectory = centralDirectoryReader.read(dataInput);


            int a = 0;
            a++;
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }

        return null;

    }
}
