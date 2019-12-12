package ru.olegcherednik.zip4jvm.model.block.crypto;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.model.block.Block;

/**
 * @author Oleg Cherednik
 * @since 23.10.2019
 */
@Getter
public class AesEncryptionHeaderBlock implements EncryptionHeaderBlock {

    private final Block salt = new Block();
    private final Block passwordChecksum = new Block();
    private final Block mac = new Block();

}
