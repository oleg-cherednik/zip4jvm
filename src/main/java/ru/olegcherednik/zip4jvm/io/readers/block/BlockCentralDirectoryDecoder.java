package ru.olegcherednik.zip4jvm.io.readers.block;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.crypto.CentralDirectoryDecoder;
import ru.olegcherednik.zip4jvm.model.block.EncryptedCentralDirectoryBlock;

import java.util.Arrays;

/**
 * @author Oleg Cherednik
 * @since 10.09.2023
 */
@RequiredArgsConstructor
public class BlockCentralDirectoryDecoder implements CentralDirectoryDecoder {

    private final CentralDirectoryDecoder decoder;
    private final EncryptedCentralDirectoryBlock block;

    // ---------- CentralDirectoryDecoder ----------

    @Override
    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public byte[] decrypt(byte[] encrypted) {
        byte[] buf = decoder.decrypt(encrypted);
        block.setDecompressedCentralDirectory(Arrays.copyOf(buf, buf.length));
        return buf;
    }
}
