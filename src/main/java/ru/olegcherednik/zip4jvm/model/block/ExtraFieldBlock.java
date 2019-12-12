package ru.olegcherednik.zip4jvm.model.block;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 14.11.2019
 */
public class ExtraFieldBlock extends Block {

    private final Map<Integer, Block> records = new LinkedHashMap<>();

    public void addRecord(int signature, Block block) {
        records.put(signature, block);
    }

    public Block getRecord(int signature) {
        return records.get(signature);
    }

}
