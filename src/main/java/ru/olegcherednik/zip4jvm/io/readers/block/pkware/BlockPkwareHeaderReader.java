package ru.olegcherednik.zip4jvm.io.readers.block.pkware;

import ru.olegcherednik.zip4jvm.crypto.pkware.PkwareHeader;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 24.10.2019
 */
public class BlockPkwareHeaderReader implements Reader<PkwareEncryptionHeader> {

    @Override
    public PkwareEncryptionHeader read(DataInput in) throws IOException {
        PkwareEncryptionHeader encryptionHeader = new PkwareEncryptionHeader();
        encryptionHeader.getData().calc(in, () -> in.readBytes(PkwareHeader.SIZE));
        return encryptionHeader;
    }
}
