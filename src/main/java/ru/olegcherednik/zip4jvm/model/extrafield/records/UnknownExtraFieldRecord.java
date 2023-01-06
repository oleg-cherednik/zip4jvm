package ru.olegcherednik.zip4jvm.model.extrafield.records;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 05.01.2023
 */
@RequiredArgsConstructor
public class UnknownExtraFieldRecord implements PkwareExtraField.Record {

    @Getter
    private final int signature;
    private final byte[] data;

    public byte[] getData() {
        return ArrayUtils.clone(data);
    }

    @Override
    public int getBlockSize() {
        return data.length;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public String getTitle() {
        return "Unknown";
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeWordSignature(signature);
        out.writeWord(data.length);
        out.write(data, 0, data.length);
    }

}
