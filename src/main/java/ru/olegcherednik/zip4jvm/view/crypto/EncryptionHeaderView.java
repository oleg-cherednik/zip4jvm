package ru.olegcherednik.zip4jvm.view.crypto;

import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.model.block.crypto.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.DecryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.EncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.PkwareEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.view.BaseView;

import java.io.PrintStream;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 24.10.2019
 */
public final class EncryptionHeaderView extends BaseView {

    private final DecryptionHeader decryptionHeader;
    private final EncryptionHeaderBlock block;
    private final long pos;

    public EncryptionHeaderView(DecryptionHeader decryptionHeader, EncryptionHeaderBlock block, long pos, int offs, int columnWidth,
            long totalDisks) {
        super(offs, columnWidth, totalDisks);
        this.decryptionHeader = decryptionHeader;
        this.block = requireNotNull(block, "EncryptionHeaderView.centralDirectory");
        this.pos = pos;
    }

    @Override
    public boolean print(PrintStream out) {
        if (block instanceof AesEncryptionHeaderBlock)
            new AesEncryptionHeaderView((AesEncryptionHeaderBlock)block, pos, offs, columnWidth, totalDisks).print(out);
        else if (block instanceof PkwareEncryptionHeaderBlock)
            new PkwareEncryptionHeaderView((PkwareEncryptionHeaderBlock)block, pos, offs, columnWidth, totalDisks).print(out);
        else if (block instanceof DecryptionHeaderBlock)
            new DecryptionHeaderView(decryptionHeader, (DecryptionHeaderBlock)block, pos, offs, columnWidth, totalDisks).print(out);

        return true;
    }

}
