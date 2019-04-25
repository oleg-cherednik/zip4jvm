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

}
