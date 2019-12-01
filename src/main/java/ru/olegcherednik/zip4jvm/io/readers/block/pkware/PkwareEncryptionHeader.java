package ru.olegcherednik.zip4jvm.io.readers.block.pkware;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.model.block.ByteArrayBlock;
import ru.olegcherednik.zip4jvm.model.block.ZipEntryBlock;

/**
 * @author Oleg Cherednik
 * @since 24.10.2019
 */
@Getter
public class PkwareEncryptionHeader implements ZipEntryBlock.EncryptionHeader {

    private final ByteArrayBlock data = new ByteArrayBlock();

}
