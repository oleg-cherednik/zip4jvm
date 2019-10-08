package ru.olegcherednik.zip4jvm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.utils.BitUtils;

import java.nio.charset.Charset;
import java.util.function.IntSupplier;

import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT0;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT1;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT11;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT13;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT2;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT3;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT6;

/**
 * see 4.4.4
 *
 * @author Oleg Cherednik
 * @since 12.03.2019
 */
@Getter
@Setter
@NoArgsConstructor
public class GeneralPurposeFlag implements IntSupplier {

    private boolean encrypted;
    private CompressionLevel compressionLevel = CompressionLevel.NORMAL;
    /** {@link DataDescriptor} */
    private boolean dataDescriptorAvailable;
    private boolean strongEncryption;
    private boolean utf8;
    private boolean encryptFileNames;

    public GeneralPurposeFlag(int data) {
        read(data);
    }

    public void read(int data) {
        encrypted = BitUtils.isBitSet(data, BIT0);
        compressionLevel = getCompressionLevel(data);
        dataDescriptorAvailable = BitUtils.isBitSet(data, BIT3);
        strongEncryption = BitUtils.isBitSet(data, BIT6);
        utf8 = BitUtils.isBitSet(data, BIT11);
        encryptFileNames = BitUtils.isBitSet(data, BIT13);
    }

    private static CompressionLevel getCompressionLevel(int data) {
        if (BitUtils.isBitSet(data, BIT1 | BIT2))
            return CompressionLevel.FASTEST;
        if (BitUtils.isBitSet(data, BIT2))
            return CompressionLevel.FAST;
        return BitUtils.isBitSet(data, BIT1) ? CompressionLevel.MAXIMUM : CompressionLevel.NORMAL;
    }

    @Override
    public int getAsInt() {
        int data = BitUtils.updateBits(0, BIT0, encrypted);
        data |= getCompressionLevelBits();
        data = BitUtils.updateBits(data, BIT3, dataDescriptorAvailable);
        data = BitUtils.updateBits(data, BIT6, strongEncryption);
        data = BitUtils.updateBits(data, BIT11, utf8);
        data = BitUtils.updateBits(data, BIT13, encryptFileNames);

        return data;
    }

    private int getCompressionLevelBits() {
        if (compressionLevel == CompressionLevel.MAXIMUM)
            return BIT1;
        if (compressionLevel == CompressionLevel.FAST)
            return BIT2;
        if (compressionLevel == CompressionLevel.FASTEST)
            return BIT1 | BIT2;
        return 0x0;
    }

    public Charset getCharset() {
        return utf8 ? Charsets.UTF_8 : Charsets.IBM437;
    }
}

/*
   7.1.6 Version 6.2 introduces support for encrypting metadata by compressing
   and encrypting the central directory data structure to reduce information
   leakage.   Information leakage can occur in legacy ZIP applications
   through exposure of information about a file even though that file is
   stored encrypted.  The information exposed consists of file
   characteristics stored within the records and fields defined by this
   specification.  This includes data such as a file's name, its original
   size, timestamp and CRC32 value.

   7.1.8 Central Directory Encryption provides greater protection against
   information leakage by encrypting the Central Directory structure and
   by masking key values that are replicated in the unencrypted Local
   Header.   ZIP compatible programs that cannot interpret an encrypted
   Central Directory structure cannot rely on the data in the corresponding
   Local Header for decompression information.

   7.1.9 Extra Field records that MAY contain information about a file that SHOULD
   not be exposed SHOULD NOT be stored in the Local Header and SHOULD only
   be written to the Central Directory where they can be encrypted.  This
   design currently does not support streaming.  Information in the End of
   Central Directory record, the Zip64 End of Central Directory Locator,
   and the Zip64 End of Central Directory records are not encrypted.  Access
   to view data on files within a ZIP file with an encrypted Central Directory
   requires the appropriate password or private key for decryption prior to
   viewing any files, or any information about the files, in the archive.

   7.1.10 Older ZIP compatible programs not familiar with the Central Directory
   Encryption feature will no longer be able to recognize the Central
   Directory and MAY assume the ZIP file is corrupt.  Programs that
   attempt streaming access using Local Headers will see invalid
   information for each file.  Central Directory Encryption need not be
   used for every ZIP file.  Its use is recommended for greater security.
   ZIP files not using Central Directory Encryption SHOULD operate as
   in the past.


7.3 Single Password - Central Directory Encryption

https://pkware.cachefly.net/webdocs/casestudies/APPNOTE.TXT


 */
