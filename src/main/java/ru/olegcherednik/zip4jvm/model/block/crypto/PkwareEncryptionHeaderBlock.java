package ru.olegcherednik.zip4jvm.model.block.crypto;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.model.block.ByteArrayBlock;

/**
 * @author Oleg Cherednik
 * @since 24.10.2019
 */
@Getter
public class PkwareEncryptionHeaderBlock implements EncryptionHeaderBlock {

    private final ByteArrayBlock data = new ByteArrayBlock();

}
