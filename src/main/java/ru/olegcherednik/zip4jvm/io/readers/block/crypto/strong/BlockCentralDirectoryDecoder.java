package ru.olegcherednik.zip4jvm.io.readers.block.crypto.strong;

import ru.olegcherednik.zip4jvm.crypto.strong.cd.CentralDirectoryDecoder;
import ru.olegcherednik.zip4jvm.model.block.crypto.EncryptedCentralDirectoryBlock;

import javax.crypto.Cipher;
import java.util.Arrays;

/**
 * @author Oleg Cherednik
 * @since 27.09.2024
 */
public class BlockCentralDirectoryDecoder extends CentralDirectoryDecoder {

    private final EncryptedCentralDirectoryBlock block;

    public BlockCentralDirectoryDecoder(Cipher cipher, EncryptedCentralDirectoryBlock block) {
        super(cipher);
        this.block = block;
    }

    @Override
    public byte[] decrypt(byte[] buf, int offs, int len) {
        byte[] res = super.decrypt(buf, offs, len);
        block.setDecompressedCentralDirectory(Arrays.copyOf(res, res.length));
        return res;
    }

}
