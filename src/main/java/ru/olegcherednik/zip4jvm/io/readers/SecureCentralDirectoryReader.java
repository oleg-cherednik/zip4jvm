package ru.olegcherednik.zip4jvm.io.readers;

import ru.olegcherednik.zip4jvm.crypto.aes.AesEngine;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrongDecoder;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeaderDecoder;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.BaseDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.LittleEndianDataInput;
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
            Cipher cipher = new DecryptionHeaderDecoder().readAndCreateCipher(in, "1".toCharArray());

//            if (expected != actual)
//                throw new IncorrectPasswordException("Central Directory");

//            AesStrongDecoder decoder = new AesStrongDecoder(cipher, (int)extensibleDataSector.getCompressedSize());

            byte[] buf = in.readBytes((int)extensibleDataSector.getCompressedSize());
            buf = cipher.update(buf);

            CentralDirectoryReader centralDirectoryReader = new CentralDirectoryReader(totalEntries, customizeCharset);
            return centralDirectoryReader.read(new LittleEndianDataInput(buf));
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }
}
