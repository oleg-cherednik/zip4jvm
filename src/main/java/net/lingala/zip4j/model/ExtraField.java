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

    public static short getExtraFieldLength(CentralDirectory.FileHeader fileHeader) {
        int extraFieldLength = 0;

        if (fileHeader.isWriteZip64FileSize())
            extraFieldLength += 16;
        if (fileHeader.isWriteZip64OffsetLocalHeader())
            extraFieldLength += 8;

        if (extraFieldLength != 0)
            extraFieldLength += 4;

        extraFieldLength += fileHeader.getExtraField().getAesExtraDataRecord() != AESExtraDataRecord.NULL ? AESExtraDataRecord.SIZE : 0;

        return (short)extraFieldLength;
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
    };


}
