package ru.olegcherednik.zip4jvm.model.block;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 14.11.2019
 */
@Getter
public class ExtraFieldBlock extends Block {

    // TODO it seems that it should be block; array is used to save into file
    private final Map<Integer, ByteArrayBlock> records = new LinkedHashMap<>();

    public void addRecord(int signature, ByteArrayBlock block) {
        records.put(signature, block);
    }

    public ByteArrayBlock getRecord(int signature) {
        return records.get(signature);
    }

}
