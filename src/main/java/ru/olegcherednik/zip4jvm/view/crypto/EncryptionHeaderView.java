package ru.olegcherednik.zip4jvm.view.crypto;

import ru.olegcherednik.zip4jvm.model.block.crypto.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.EncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.PkwareEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 24.10.2019
 */
public final class EncryptionHeaderView extends View {

    private final EncryptionHeaderBlock encryptionHeaderBlock;
    private final long pos;

    public EncryptionHeaderView(EncryptionHeaderBlock encryptionHeaderBlock, long pos, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.encryptionHeaderBlock = encryptionHeaderBlock;
        this.pos = pos;

        Objects.requireNonNull(encryptionHeaderBlock, "'encryptionHeader' must not be null");
    }

    @Override
    public boolean print(PrintStream out) {
        if (encryptionHeaderBlock instanceof AesEncryptionHeaderBlock)
            createView((AesEncryptionHeaderBlock)encryptionHeaderBlock).print(out);
        else if (encryptionHeaderBlock instanceof PkwareEncryptionHeaderBlock)
            createView((PkwareEncryptionHeaderBlock)encryptionHeaderBlock).print(out);
        // TODO add for unknown encryption header

        return true;
    }

    public BlockAesEncryptionHeaderView createView(AesEncryptionHeaderBlock encryptionHeader) {
        return new BlockAesEncryptionHeaderView(encryptionHeader, pos, offs, columnWidth);
    }

    public PkwareEncryptionHeaderView createView(PkwareEncryptionHeaderBlock encryptionHeader) {
        return new PkwareEncryptionHeaderView(encryptionHeader, pos, offs, columnWidth);
    }
}
