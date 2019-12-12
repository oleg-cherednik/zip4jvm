package ru.olegcherednik.zip4jvm.view.crypto;

import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.crypto.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.EncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.PkwareEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 24.10.2019
 */
public final class EncryptionHeaderView extends View {

    private final EncryptionHeaderBlock block;
    private final Function<Block, byte[]> getDataFunc;
    private final long pos;

    public EncryptionHeaderView(EncryptionHeaderBlock block, Function<Block, byte[]> getDataFunc, long pos, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.block = block;
        this.getDataFunc = getDataFunc;
        this.pos = pos;

        Objects.requireNonNull(block, "'block' must not be null");
    }

    @Override
    public boolean print(PrintStream out) {
        if (block instanceof AesEncryptionHeaderBlock)
            createView((AesEncryptionHeaderBlock)block).print(out);
        else if (block instanceof PkwareEncryptionHeaderBlock)
            createView((PkwareEncryptionHeaderBlock)block).print(out);
        // TODO add for unknown encryption header

        return true;
    }

    public BlockAesEncryptionHeaderView createView(AesEncryptionHeaderBlock encryptionHeader) {
        return new BlockAesEncryptionHeaderView(encryptionHeader, getDataFunc, pos, offs, columnWidth);
    }

    public PkwareEncryptionHeaderView createView(PkwareEncryptionHeaderBlock encryptionHeader) {
        return new PkwareEncryptionHeaderView(encryptionHeader, pos, offs, columnWidth);
    }
}
