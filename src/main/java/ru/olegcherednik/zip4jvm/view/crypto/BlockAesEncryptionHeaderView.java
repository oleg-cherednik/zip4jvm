package ru.olegcherednik.zip4jvm.view.crypto;

import ru.olegcherednik.zip4jvm.model.block.crypto.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;

import java.io.PrintStream;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
final class BlockAesEncryptionHeaderView extends BaseView {

    private final AesEncryptionHeaderBlock block;
    private final long pos;

    public BlockAesEncryptionHeaderView(AesEncryptionHeaderBlock block, long pos, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.block = requireNotNull(block, "BlockAesEncryptionHeaderView.centralDirectory");
        this.pos = pos;
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
        new ByteArrayHexView(block.getSalt().getData(), offs, columnWidth).print(out);
    }

    private void printPasswordChecksum(PrintStream out) {
        printValueLocation(out, "password checksum:", block.getPasswordChecksum());
        new ByteArrayHexView(block.getPasswordChecksum().getData(), offs, columnWidth).print(out);
    }

    private void printMac(PrintStream out) {
        printValueLocation(out, "mac:", block.getMac());
        new ByteArrayHexView(block.getMac().getData(), offs, columnWidth).print(out);
    }
}
