package ru.olegcherednik.zip4jvm.view.crypto;

import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.crypto.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
public final class BlockAesEncryptionHeaderView extends View {

    private final AesEncryptionHeaderBlock block;
    private final Function<Block, byte[]> getDataFunc;
    private final long pos;

    public BlockAesEncryptionHeaderView(AesEncryptionHeaderBlock block, Function<Block, byte[]> getDataFunc, long pos, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.block = block;
        this.getDataFunc = getDataFunc;
        this.pos = pos;

        Objects.requireNonNull(block, "'encryptionHeader' must not be null");
        Objects.requireNonNull(getDataFunc, "'getDataFunc' must not be null");
    }

    @Override
    public boolean print(PrintStream out) {
        printSubTitle(out, pos, "(AES) encryption header");
        printSalt(out);
        printPasswordChecksum(out);
        printMac(out);
        return true;
    }

    private void printSalt(PrintStream out) {
        printValueLocation(out, "salt:", block.getSalt());
        new ByteArrayHexView(getDataFunc.apply(block.getSalt()), offs, columnWidth).print(out);
    }

    private void printPasswordChecksum(PrintStream out) {
        printValueLocation(out, "password checksum:", block.getPasswordChecksum());
        new ByteArrayHexView(getDataFunc.apply(block.getPasswordChecksum()), offs, columnWidth).print(out);
    }

    private void printMac(PrintStream out) {
        printValueLocation(out, "mac:", block.getMac());
        new ByteArrayHexView(getDataFunc.apply(block.getMac()), offs, columnWidth).print(out);
    }
}
