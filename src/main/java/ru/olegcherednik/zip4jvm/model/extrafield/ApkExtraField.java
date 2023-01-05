package ru.olegcherednik.zip4jvm.model.extrafield;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 05.01.2023
 */
@Getter
@RequiredArgsConstructor
public class ApkExtraField extends ExtraField {

    private final byte[] data;

    @Override
    public int getTotalRecords() {
        return 1;
    }
}
