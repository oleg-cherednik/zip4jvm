package ru.olegcherednik.zip4jvm.model.block.crypto;

import ru.olegcherednik.zip4jvm.model.block.Block;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 31.03.2020
 */
public class RecipientsBlock extends Block {

    private final Map<Integer, Block> recipients = new HashMap<>();

    public Block getRecipient(int num) {
        return recipients.getOrDefault(num, Block.NULL);
    }

    public void addRecipient(int num, Block block) {
        recipients.put(num, block);
    }

}
