package net.lingala.zip4j.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@Getter
@Setter
public class ExtraField {

    private Zip64ExtendedInfo zip64ExtendedInfo;
    private AESExtraDataRecord aesExtraDataRecord;

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

        extraFieldLength += fileHeader.getAesExtraDataRecord() != null ? AESExtraDataRecord.SIZE : 0;

        return (short)extraFieldLength;
    }

}
