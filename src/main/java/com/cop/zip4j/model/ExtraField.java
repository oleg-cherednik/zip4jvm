package com.cop.zip4j.model;

import com.cop.zip4j.model.aes.AesExtraDataRecord;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@Getter
@Setter
public class ExtraField {

    public static final int NO_DATA = -1;

    @NonNull
    private Zip64.ExtendedInfo extendedInfo = Zip64.ExtendedInfo.NULL;
    @NonNull
    private AesExtraDataRecord aesExtraDataRecord = AesExtraDataRecord.NULL;

    public boolean isEmpty() {
        return extendedInfo == null && aesExtraDataRecord == null;
    }

    public int getLength() {
        return extendedInfo.getLength() + aesExtraDataRecord.getLength();
    }

    @NonNull
    public ExtraField deepCopy() {
        ExtraField res = new ExtraField();

        if (extendedInfo != Zip64.ExtendedInfo.NULL)
            res.setExtendedInfo(extendedInfo.toBuilder().build());
        if (aesExtraDataRecord != AesExtraDataRecord.NULL)
            res.setAesExtraDataRecord(aesExtraDataRecord.toBuilder().build());

        return res;
    }

    public static final ExtraField NULL = new ExtraField() {
        @Override
        public void setExtendedInfo(@NonNull Zip64.ExtendedInfo extendedInfo) {
            throw new NullPointerException("Null object modification: " + getClass().getSimpleName());
        }

        @Override
        public void setAesExtraDataRecord(@NonNull AesExtraDataRecord aesExtraDataRecord) {
            throw new NullPointerException("Null object modification: " + getClass().getSimpleName());
        }

        @Override
        public ExtraField deepCopy() {
            return NULL;
        }

        @Override
        public String toString() {
            return "<null>";
        }
    };

}
