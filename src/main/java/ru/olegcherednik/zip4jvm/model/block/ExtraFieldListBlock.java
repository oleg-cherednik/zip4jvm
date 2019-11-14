package ru.olegcherednik.zip4jvm.model.block;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 14.11.2019
 */
@Getter
public class ExtraFieldListBlock extends Block {

    private final Map<Integer, ByteArrayBlock> records = new LinkedHashMap<>();

    @Setter(AccessLevel.NONE)
    private ByteArrayBlock record;

    public void addRecord() {
        record = new ByteArrayBlock();
    }

    public void saveRecord(int signature) {
        records.put(signature, record);
        record = null;
    }

    public ByteArrayBlock getRecord(int signature) {
        return records.get(signature);
    }

}
