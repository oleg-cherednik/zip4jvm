package ru.olegcherednik.zip4jvm.view.crypto;

import ru.olegcherednik.zip4jvm.model.block.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.PkwareEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.ZipEntryBlock;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 24.10.2019
 */
public final class EncryptionHeaderView extends View {

    private final ZipEntryBlock.EncryptionHeader encryptionHeader;
    private final long pos;

    public EncryptionHeaderView(ZipEntryBlock.EncryptionHeader encryptionHeader, long pos, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.encryptionHeader = encryptionHeader;
        this.pos = pos;

        Objects.requireNonNull(encryptionHeader, "'encryptionHeader' must not be null");
    }

    @Override
    public boolean print(PrintStream out) {
        if (encryptionHeader instanceof AesEncryptionHeaderBlock)
            createView((AesEncryptionHeaderBlock)encryptionHeader).print(out);
        else if (encryptionHeader instanceof PkwareEncryptionHeaderBlock)
            createView((PkwareEncryptionHeaderBlock)encryptionHeader).print(out);
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
