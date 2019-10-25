package ru.olegcherednik.zip4jvm.io.readers.block.pkware;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;

/**
 * @author Oleg Cherednik
 * @since 24.10.2019
 */
@Getter
public class PkwareEncryptionHeader implements Diagnostic.ZipEntryBlock.EncryptionHeader {

    private final Diagnostic.ByteArrayBlock data = new Diagnostic.ByteArrayBlock();

}
