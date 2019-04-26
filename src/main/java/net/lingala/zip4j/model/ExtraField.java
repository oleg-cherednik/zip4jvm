package net.lingala.zip4j.model;

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
    private Zip64ExtendedInfo zip64ExtendedInfo = Zip64ExtendedInfo.NULL;
    @NonNull
    private AESExtraDataRecord aesExtraDataRecord = AESExtraDataRecord.NULL;

    public boolean isEmpty() {
        return zip64ExtendedInfo == null && aesExtraDataRecord == null;
    }

    public int getLength() {
        return zip64ExtendedInfo.getLength() + aesExtraDataRecord.getLength();
    }

    @NonNull
    public ExtraField deepCopy() {
        ExtraField res = new ExtraField();

        if (zip64ExtendedInfo != Zip64ExtendedInfo.NULL)
            res.setZip64ExtendedInfo(zip64ExtendedInfo.toBuilder().build());
        if (aesExtraDataRecord != AESExtraDataRecord.NULL)
            res.setAesExtraDataRecord(aesExtraDataRecord.toBuilder().build());

        return res;
    }

    public static final ExtraField NULL = new ExtraField() {
        @Override
        public void setZip64ExtendedInfo(@NonNull Zip64ExtendedInfo zip64ExtendedInfo) {
            throw new NullPointerException("Null object modification: " + getClass().getSimpleName());
        }

        @Override
        public void setAesExtraDataRecord(@NonNull AESExtraDataRecord aesExtraDataRecord) {
            throw new NullPointerException("Null object modification: " + getClass().getSimpleName());
        }

        @Override
        public ExtraField deepCopy() {
            return NULL;
        }
    };

}
