package ru.olegcherednik.zip4jvm.model.block;

import lombok.Getter;

/**
 * @author Oleg Cherednik
 * @since 24.10.2019
 */
@Getter
public class PkwareEncryptionHeaderBlock implements ZipEntryBlock.EncryptionHeader {

    private final ByteArrayBlock data = new ByteArrayBlock();

}
