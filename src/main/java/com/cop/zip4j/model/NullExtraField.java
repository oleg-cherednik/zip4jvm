package com.cop.zip4j.model;

import com.cop.zip4j.model.aes.AesExtraDataRecord;
import lombok.NonNull;

/**
 * @author Oleg Cherednik
 * @since 21.08.2019
 */
public final class NullExtraField extends ExtraField {

    @Override
    public void setExtendedInfo(@NonNull Zip64.ExtendedInfo extendedInfo) {
        throw new NullPointerException("Null object modification: " + getClass().getSimpleName());
    }

    @Override
    public void setAesExtraDataRecord(@NonNull AesExtraDataRecord aesExtraDataRecord) {
        throw new NullPointerException("Null object modification: " + getClass().getSimpleName());
    }

    @Override
    public String toString() {
        return "<null>";
    }

}
