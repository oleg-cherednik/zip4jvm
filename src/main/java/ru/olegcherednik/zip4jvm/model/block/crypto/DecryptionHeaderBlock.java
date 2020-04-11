package ru.olegcherednik.zip4jvm.model.block.crypto;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.model.block.Block;

/**
 * @author Oleg Cherednik
 * @since 30.03.2020
 */
@Getter
public class DecryptionHeaderBlock extends Block implements EncryptionHeaderBlock {

    private final RecipientsBlock recipientsBlock = new RecipientsBlock();

}
