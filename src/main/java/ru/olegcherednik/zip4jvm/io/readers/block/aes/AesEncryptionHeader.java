package ru.olegcherednik.zip4jvm.io.readers.block.aes;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;

/**
 * @author Oleg Cherednik
 * @since 23.10.2019
 */
@Getter
public class AesEncryptionHeader implements Diagnostic.ZipEntryBlock.EncryptionHeader {

    private final Block salt = new Block();
    private final Block passwordChecksum = new Block();
    private final Block mac = new Block();

}
