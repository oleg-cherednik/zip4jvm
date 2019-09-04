package ru.olegcherednik.zip4jvm.model;

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

    public int getSize() {
        return extendedInfo.getBlockSize() + aesExtraDataRecord.getBlockSize();
    }

    public void setFrom(@NonNull ExtraField extraField) {
        extendedInfo = extraField.extendedInfo;
        aesExtraDataRecord = extraField.aesExtraDataRecord;
    }

}
