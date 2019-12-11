package ru.olegcherednik.zip4jvm.model.block.crypto;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.model.block.ByteArrayBlock;

/**
 * @author Oleg Cherednik
 * @since 23.10.2019
 */
@Getter
public class AesEncryptionHeaderBlock implements EncryptionHeaderBlock {

    private final ByteArrayBlock salt = new ByteArrayBlock();
    private final ByteArrayBlock passwordChecksum = new ByteArrayBlock();
    private final ByteArrayBlock mac = new ByteArrayBlock();

}
