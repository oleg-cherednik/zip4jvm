package ru.olegcherednik.zip4jvm.model;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.crypto.Encoder;
import ru.olegcherednik.zip4jvm.crypto.aes.AesDecoder;
import ru.olegcherednik.zip4jvm.crypto.aes.AesEncoder;
import ru.olegcherednik.zip4jvm.crypto.aes.AesEngine;
import ru.olegcherednik.zip4jvm.crypto.pkware.PkwareDecoder;
import ru.olegcherednik.zip4jvm.crypto.pkware.PkwareEncoder;
import ru.olegcherednik.zip4jvm.crypto.tripledes.TripleDesDecoder;
import ru.olegcherednik.zip4jvm.exception.EncryptionNotSupportedException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.entry.RegularFileZipEntry;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.extrafield.AesExtraFieldRecord;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 16.02.2020
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum EncryptionMethod {
    OFF(zipEntry -> Encoder.NULL, (zipEntry, in) -> Decoder.NULL, ZipEntry::getChecksum),
    PKWARE(PkwareEncoder::create, PkwareDecoder::create, ZipEntry::getChecksum),
    AES_128(AesEncoder::create, AesDecoder::create, entry -> 0L),
    AES_192(AES_128.createEncoder, AES_128.createDecoder, AES_128.checksum),
    AES_256(AES_128.createEncoder, AES_128.createDecoder, AES_128.checksum),
    DES(null, null, ZipEntry::getChecksum),
    RC2_PRE_52(null, null, ZipEntry::getChecksum),
    TRIPLE_DES_168(null, TripleDesDecoder::create, ZipEntry::getChecksum),
    TRIPLE_DES_192(null, TRIPLE_DES_168.createDecoder, ZipEntry::getChecksum),
    RC2(null, null, ZipEntry::getChecksum),
    RC4(null, null, ZipEntry::getChecksum),
    BLOWFISH(null, null, ZipEntry::getChecksum),
    TWOFISH(null, null, ZipEntry::getChecksum),
    UNKNOWN(null, null, ZipEntry::getChecksum);

    private final Function<ZipEntry, Encoder> createEncoder;
    private final CreateDecoder createDecoder;
    private final Function<ZipEntry, Long> checksum;

    public final Encoder createEncoder(RegularFileZipEntry zipEntry) {
        return Optional.ofNullable(createEncoder).orElseThrow(() -> new EncryptionNotSupportedException(this)).apply(zipEntry);
    }

    public final Decoder createDecoder(RegularFileZipEntry zipEntry, DataInput in) throws IOException {
        return Optional.ofNullable(createDecoder).orElseThrow(() -> new EncryptionNotSupportedException(this)).apply(zipEntry, in);
    }

    public final long getChecksum(ZipEntry zipEntry) {
        return checksum.apply(zipEntry);
    }

    public final boolean isAes() {
        return this == AES_128 || this == AES_192 || this == AES_256;
    }

    public static EncryptionMethod get(ExtraField extraField, GeneralPurposeFlag generalPurposeFlag) {
        if (!generalPurposeFlag.isEncrypted())
            return OFF;
        if (extraField.getAesRecord() != AesExtraFieldRecord.NULL)
            return AesEngine.getEncryption(extraField.getAesRecord().getStrength());
        if (generalPurposeFlag.isStrongEncryption())
            return extraField.getAlgIdRecord().getEncryptionAlgorithm().getEncryptionMethod();
        return PKWARE;
    }

    private interface CreateDecoder {

        Decoder apply(ZipEntry zipEntry, DataInput in) throws IOException;
    }
}
