package com.cop.zip4j.model.aes;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.model.CompressionMethod;
import com.cop.zip4j.model.ExtraField;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang.ArrayUtils;

import java.nio.charset.Charset;

@Getter
@Builder
public final class AesExtraDataRecord {

    public static final int SIGNATURE = 0x9901;
    public static final int SIZE = 2 + 2 + 2 + 2 + 1 + 2;   // size:11
    public static final int SIZE_FIELD = 2 + 2; // 4 bytes: signature + size

    public static final AesExtraDataRecord NULL = builder().build();

    // size:2 - signature (0x9901)
    // size:2
    @Builder.Default
    private final int size = ExtraField.NO_DATA;
    // size:2
    @Builder.Default
    private final int versionNumber = ExtraField.NO_DATA;
    // size:2
    private final String vendor;
    // size:1
    @NonNull
    @Builder.Default
    private final AesStrength strength = AesStrength.NONE;
    // size:2
    @NonNull
    @Builder.Default
    private final CompressionMethod compressionMethod = CompressionMethod.STORE;

    // TODO should be checked on set
    public byte[] getVendor(@NonNull Charset charset) {
        byte[] buf = vendor != null ? vendor.getBytes(charset) : null;

        if (ArrayUtils.getLength(buf) > 2)
            throw new Zip4jException("AESExtraDataRecord.vendor should be maximum 2 characters");

        return buf;
    }

    public int getBlockSize() {
        return this == NULL ? 0 : SIZE;
    }

    @Override
    public String toString() {
        return this == NULL ? "<null>" : super.toString();
    }

}
