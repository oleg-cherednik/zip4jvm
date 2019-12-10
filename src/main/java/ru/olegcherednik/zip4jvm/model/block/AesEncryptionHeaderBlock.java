package ru.olegcherednik.zip4jvm.model.block;

import lombok.Getter;

/**
 * @author Oleg Cherednik
 * @since 23.10.2019
 */
@Getter
public class AesEncryptionHeaderBlock implements ZipEntryBlock.EncryptionHeader {

    private final ByteArrayBlock salt = new ByteArrayBlock();
    private final ByteArrayBlock passwordChecksum = new ByteArrayBlock();
    private final ByteArrayBlock mac = new ByteArrayBlock();

}
