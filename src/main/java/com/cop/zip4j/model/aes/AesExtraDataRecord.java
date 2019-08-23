package com.cop.zip4j.model.aes;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.model.CompressionMethod;
import com.cop.zip4j.model.ExtraField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang.ArrayUtils;

import java.nio.charset.Charset;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AesExtraDataRecord {

    public static final int SIGNATURE = 0x9901;
    public static final int SIZE = 2 + 2 + 2 + 2 + 1 + 2;   // size:11
    public static final int SIZE_FIELD = 2 + 2; // 4 bytes: signature + size

    // size:2 - signature (0x9901)
    // size:2
    @Builder.Default
    private int dataSize = ExtraField.NO_DATA;
    // size:2
    @Builder.Default
    private int versionNumber = ExtraField.NO_DATA;
    // size:2
    private String vendor;
    // size:1
    @NonNull
    @Builder.Default
    private AesStrength strength = AesStrength.NONE;
    // size:2
    @NonNull
    @Builder.Default
    private CompressionMethod compressionMethod = CompressionMethod.STORE;

    // TODO should be checked on set
    public byte[] getVendor(@NonNull Charset charset) {
        byte[] buf = vendor != null ? vendor.getBytes(charset) : null;

        if (ArrayUtils.getLength(buf) > 2)
            throw new Zip4jException("AESExtraDataRecord.vendor should be maximum 2 characters");

        return buf;
    }

    public int getSize() {
        return SIZE;
    }

    public static final AesExtraDataRecord NULL = new AesExtraDataRecord() {

        @Override
        public void setDataSize(int dataSize) {
            throw new NullPointerException("Null object modification: " + getClass().getSimpleName());
        }

        @Override
        public void setVersionNumber(int versionNumber) {
            throw new NullPointerException("Null object modification: " + getClass().getSimpleName());
        }

        @Override
        public void setVendor(String vendor) {
            throw new NullPointerException("Null object modification: " + getClass().getSimpleName());
        }

        @Override
        public void setStrength(AesStrength strength) {
            throw new NullPointerException("Null object modification: " + getClass().getSimpleName());
        }

        @Override
        public void setCompressionMethod(CompressionMethod compressionMethod) {
            throw new NullPointerException("Null object modification: " + getClass().getSimpleName());
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public String toString() {
            return "<null>";
        }
    };

}
