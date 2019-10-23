package ru.olegcherednik.zip4jvm.io.readers.block.aes;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;

/**
 * @author Oleg Cherednik
 * @since 23.10.2019
 */
@Getter
public class AesEncryptionHeader implements Diagnostic.ZipEntryBlock.EncryptionHeader {

    private final Diagnostic.ByteArrayBlock salt = new Diagnostic.ByteArrayBlock();
    private final Diagnostic.ByteArrayBlock passwordChecksum = new Diagnostic.ByteArrayBlock();
    private final Diagnostic.ByteArrayBlock mac = new Diagnostic.ByteArrayBlock();

}
