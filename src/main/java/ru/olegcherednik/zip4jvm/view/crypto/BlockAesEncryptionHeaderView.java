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

    private final AesEncryptionHeaderBlock encryptionHeader;
    private final Function<Block, byte[]> getDataFunc;
    private final long pos;

    public BlockAesEncryptionHeaderView(AesEncryptionHeaderBlock encryptionHeader, Function<Block, byte[]> getDataFunc, long pos, int offs,
            int columnWidth) {
        super(offs, columnWidth);
        this.encryptionHeader = encryptionHeader;
        this.getDataFunc = getDataFunc;
        this.pos = pos;

        Objects.requireNonNull(encryptionHeader, "'encryptionHeader' must not be null");
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
        printValueLocation(out, "salt:", encryptionHeader.getSalt());
        new ByteArrayHexView(getDataFunc.apply(encryptionHeader.getSalt()), offs, columnWidth).print(out);
    }

    private void printPasswordChecksum(PrintStream out) {
        printValueLocation(out, "password checksum:", encryptionHeader.getPasswordChecksum());
        new ByteArrayHexView(getDataFunc.apply(encryptionHeader.getPasswordChecksum()), offs, columnWidth).print(out);
    }

    private void printMac(PrintStream out) {
        printValueLocation(out, "mac:", encryptionHeader.getMac());
        new ByteArrayHexView(getDataFunc.apply(encryptionHeader.getMac()), offs, columnWidth).print(out);
    }
}
