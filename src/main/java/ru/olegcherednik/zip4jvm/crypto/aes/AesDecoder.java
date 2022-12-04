/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.crypto.aes;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.zip.CRC32;

import static ru.olegcherednik.zip4jvm.crypto.aes.AesEngine.MAC_SIZE;
import static ru.olegcherednik.zip4jvm.crypto.aes.AesEngine.PASSWORD_CHECKSUM_SIZE;

/**
 * @author Oleg Cherednik
 * @since 13.08.2019
 */
public final class AesDecoder implements Decoder {

    private static final int SHA1_NUM_DIGEST_WORDS = 5;
    private static final int SHA1_DIGEST_SIZE = SHA1_NUM_DIGEST_WORDS * 4;

    private static final int kDigestSize = SHA1_DIGEST_SIZE;

    private final int saltLength;
    private final AesEngine engine;

    public static AesDecoder create(ZipEntry zipEntry, DataInput in) throws IOException {
        try {
            if (zipEntry.isStrongEncryption()) {
                DecryptionHeader decryptionHeader = new DecryptionHeaderReader().read(in);
                // decryptionHeader.getVersion() should be 3

//                int algId = decryptionHeader.getEncryptionAlgorithmCode();
//                algId -= EncryptionAlgorithm.AES_128.getCode();
//                AesStrength strength = AesStrength.S256;
//                int _keyKeySize = 16 + algId * 8;  //32 strength.keyLength()
/*
   ee a6 b4 f2   2c 84 88 da   f5 08 cf 3f   8b 23 3a 7b   │ ····,······?·#:{ │
   7c fd 6f 3c   4a d8 6b 34   3f dc 37 ab   56 ba 29 2d   │ |·o<J·k4?·7·V·)- │
   59 af 53 ee   7f 1d 7c 28   2d b8 8f 37   a6 f1 27 3e   │ Y·S···|(-··7··'> │
   98 fe e3 8c   c1 54 c6 d6   7b 46 c9 ad   92 2a 8a cc   │ ·····T··{F···*·· │
   da 78 60 e8   b0 2b 98 fb   f2 64 25 9b   d8 ce 97 87   │ ·x`··+···d%····· │
   b4 da a7 53   26 e1 e1 44   61 d5 13 d6   f7 f7 9b a1   │ ···S&··Da······· │
   5c fa 2b 84   64 9b 5d da   6a 5b 16 dc   b5 34 2d 4c   │ \·+·d·]·j[···4-L │
   3f 56 f5 87   e1 40 87 4c   e1 76 dd 66   90 fa 70 f6   │ ?V···@·L·v·f··p· │
   ca 9b a8 9e   5b f8 50 46   99 d0 0a fd   02 87 7d 80   │ ····[·PF······}· │
 */
                MyAes aes = new MyAes();
                aes.init(false, 0);
                aes.SetKey(getMasterKey(zipEntry.getPassword()));
                aes.SetInitVector(decryptionHeader.getIv());

/*
   de 57 85 89   63 e2 0b 28   2b 4e 12 df   66 21 08 e3   │ ·W··c··(+N··f!·· │
   b7 58 d9 d9   6c 86 95 31   15 79 4a 41   5c 55 17 5f   │ ·X··l··1·yJA\U·_ │
   6f f8 8a 18   ab d4 39 d2   cc 9c 0a 66   7c 68 5c 9c   │ o·····9····f|h\· │
   cf 46 d5 c7   67 80 c8 ed   ef c6 20 ec   9d 0d 22 41   │ ·F··g····· ···"A │
   4d cf 24 5e   32 9b 59 c3   bc 36 a8 60   4b 9f 09 e2   │ M·$^2·Y··6·`K··· │
   6f c1 c8 87   e1 3c 06 22   7f 99 21 68   21 d2 df 94   │ o····<·"··!h!··· │
   07 0f 09 59   a4 9d 59 6e   93 0c 8c 4d   b8 65 b2 8a   │ ···Y··Yn···M·e·· │
   e5 c7 91 eb   08 2a 0e 69   77 fe 1d 93   b8 a0 01 86   │ ·····*·iw······· │
   10 10 10 10   10 10 10 10   10 10 10 10   10 10 10 10   │ ················ │
 */
                byte[] iv = Arrays.copyOf(decryptionHeader.getIv(), decryptionHeader.getIv().length);
                byte[] randomData = decryptRandomData(decryptionHeader, zipEntry.getPassword());
                byte[] fileKey1 = getFileKey(decryptionHeader, randomData);
                byte[] fileKey2 = Arrays.copyOf(fileKey1, fileKey1.length);

                aes.SetKey(fileKey1);
                aes.SetInitVector(decryptionHeader.getIv());

                int validSize = decryptionHeader.getPasswordValidationData().length;
                byte[] pwd = new byte[validSize + 4];
                System.arraycopy(decryptionHeader.getPasswordValidationData(), 0, pwd, 0, validSize);
                MyAes.SetUi32(pwd, validSize, (int)decryptionHeader.getCrc32());

                byte[] decrypted2 = aes.filter(pwd);

                Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
                SecretKeySpec secretKey = new SecretKeySpec(fileKey2, "AES");
                cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
                byte[] decryptedData = cipher.doFinal(pwd);


                CRC32 crc = new CRC32();
                crc.update(decrypted2, 0, validSize);

                if (MyAes.GetUi32(decryptedData, validSize) != crc.getValue())
                    throw new RuntimeException();

                int a = 0;
                a++;

// password validation data
/*
   f1 12 37 d8   25 40 91 69   14 a0 16 74   ac 6c 07 8d   │ ··7·%@·i···t·l·· │
   63 e5 06 57   cb 7c a1 c5   41 b4 5f fa   41 be 43 07   │ c··W·|··A·_·A·C· │
   52 11 24 c5   4d 5b 32 97   1a 19 20 a4   72 a2 f7 71   │ R·$·M[2··· ·r··q │
   37 be 24 f3   77 56 e2 10   11 8e 6b d7   20 0e 21 5f   │ 7·$·wV····k· ·!_ │
   3c 35 59 4d   6c b1 80 57   f5 fe b1 59   7e 86 5c a8   │ <5YMl··W···Y~·\· │
   fa 20 32 dd   c4 df 92 3a   28 fc 45 8c   2e 59 1d 1b   │ · 2····:(·E·.Y·· │
   49 00 a4 26   39 ea c5 2f   02 8d 81 09   02 67 05 38   │ I··&9··/·····g·8 │
   25 aa 83 41   66 a8 6d 2e   59 d6 b7 3e   61 8a 8b a6   │ %··Af·m.Y··>a··· │
 */

// decrypted password validation data

/*
   1f 7b ac 10   29 d3 8b e1   d8 e3 ff 72   0a 9c b8 7b   │ ·{··)······r···{ │
   56 63 9c 5a   9e 85 54 f7   40 07 cd 27   88 07 b4 85   │ Vc·Z··T·@··'···· │
   03 06 45 30   ef 44 b6 c7   b0 92 90 5b   43 8c 39 83   │ ··E0·D·····[C·9· │
   fa 3c f4 8f   64 6a e3 cc   92 5e e1 94   7e d2 1e 09   │ ·<··dj···^··~··· │
   35 ef 18 19   6a 0f df 4a   70 6f fb f3   17 f1 6c d7   │ 5···j··Jpo····l· │
   3a 6d 1f ef   f3 c1 d6 0c   fb f2 57 fd   43 1d 00 05   │ :m········W·C··· │
   7c 5e e9 46   fc 5e 14 39   c3 10 dc 09   bc d2 7e 9a   │ |^·F·^·9······~· │
   e1 a8 86 ef   37 62 48 13   54 4c 32 40   55 9b 8c 4c   │ ····7bH·TL2@U··L │
 */

//                strength = AesEngine.getStrength(zipEntry.getEncryptionMethod());
//                byte[] salt = iv;
//                key = AesEngine.createKey(zipEntry.getPassword(), salt, strength);

//                Cipher cipher = AesEngine.createCipher(strength.createSecretKeyForCipher(key));
//                Mac mac = AesEngine.createMac(strength.createSecretKeyForMac(key));

                return null;
            } else {
                AesStrength strength = AesEngine.getStrength(zipEntry.getEncryptionMethod());
                byte[] salt = in.readBytes(strength.saltLength());
                byte[] key = AesEngine.createKey(zipEntry.getPassword(), salt, strength);

                Cipher cipher = AesEngine.createCipher(strength.createSecretKeyForCipher(key));
                Mac mac = AesEngine.createMac(strength.createSecretKeyForMac(key));
                byte[] passwordChecksum = strength.createPasswordChecksum(key);

                checkPasswordChecksum(passwordChecksum, zipEntry, in);

                return new AesDecoder(cipher, mac, salt.length);
            }
        } catch(Zip4jvmException | IOException e) {
            throw e;
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    private static byte[] decryptRandomData(DecryptionHeader decryptionHeader, char[] password) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKey secretKey = new SecretKeySpec(getMasterKey(password), "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(decryptionHeader.getIv()));
        return cipher.doFinal(decryptionHeader.getEncryptedRandomData());
    }

//    private static byte[] decryptRandomData(byte[] encryptedData, byte[] key, byte[] iv) throws Exception {
//        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//        SecretKey secretKey = new SecretKeySpec(key, "AES");
//        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
//        return cipher.doFinal(encryptedData);
//    }

    private static byte[] convert(String str, int size) {
        byte[] res = new byte[size];

        Scanner scan = new Scanner(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)));
        int i = 0;

        out:
        while (scan.hasNext()) {
            for (String s : scan.nextLine().split("\\s+")) {
                res[i++] = (byte)Integer.parseInt(s, 16);

                if (i >= res.length)
                    break out;
            }
        }

        return res;
    }

    private static byte[] getMasterKey(char[] password) {
        byte[] data = new String(password).getBytes(StandardCharsets.UTF_8);
        byte[] sha1 = DigestUtils.sha1(data);
        return DeriveKey(sha1);
    }

    private static byte[] getFileKey(DecryptionHeader decryptionHeader, byte[] randomData) {
        MessageDigest md = DigestUtils.getSha1Digest();
        md.update(decryptionHeader.getIv());
        md.update(randomData);
        return DeriveKey(md.digest());
    }

    private static byte[] DeriveKey(byte[] digest) {
        byte[] buf = new byte[kDigestSize * 2];  // kDigestSize = 20
        DeriveKey2(digest, (byte)0x36, buf, 0);
        DeriveKey2(digest, (byte)0x5C, buf, kDigestSize);
        return Arrays.copyOfRange(buf, 0, 32);
    }

    private static void DeriveKey2(byte[] digest, byte c, byte[] dest, int offs) {
        byte[] buf = new byte[64];
        Arrays.fill(buf, c);

        for (int i = 0; i < kDigestSize; i++)
            buf[i] ^= digest[i];

        byte[] sha1 = DigestUtils.sha1(buf);
        System.arraycopy(sha1, 0, dest, offs, sha1.length);
    }

    private AesDecoder(Cipher cipher, Mac mac, int saltLength) {
        this.saltLength = saltLength;
        engine = new AesEngine(cipher, mac);
    }

    @Override
    public void decrypt(byte[] buf, int offs, int len) {
        try {
            engine.updateMac(buf, offs, len);
            engine.cypherUpdate(buf, offs, len);
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    @Override
    public long getDataCompressedSize(long compressedSize) {
        return AesEngine.getDataCompressedSize(compressedSize, saltLength);
    }

    @Override
    public void close(DataInput in) throws IOException {
        checkMessageAuthenticationCode(in);
    }

    private void checkMessageAuthenticationCode(DataInput in) throws IOException {
        byte[] expected = in.readBytes(MAC_SIZE);
        byte[] actual = ArrayUtils.subarray(engine.getMac(), 0, MAC_SIZE);

        if (!ArrayUtils.isEquals(expected, actual))
            throw new Zip4jvmException("Message Authentication Code (MAC) is incorrect");
    }

    private static void checkPasswordChecksum(byte[] actual, ZipEntry entry, DataInput in) throws IOException {
        byte[] expected = in.readBytes(PASSWORD_CHECKSUM_SIZE);

        if (!Objects.deepEquals(expected, actual))
            throw new IncorrectPasswordException(entry.getFileName());
    }

}
