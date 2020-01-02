package ru.olegcherednik.zip4jvm.view.crypto;

import ru.olegcherednik.zip4jvm.model.block.crypto.PkwareEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;

import java.io.PrintStream;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
final class PkwareEncryptionHeaderView extends BaseView {

    private final PkwareEncryptionHeaderBlock block;
    private final long pos;

    public PkwareEncryptionHeaderView(PkwareEncryptionHeaderBlock block, long pos, int offs, int columnWidth, long totalDisks) {
        super(offs, columnWidth, totalDisks);
        this.block = requireNotNull(block, "PkwareEncryptionHeaderView.centralDirectory");
        this.pos = pos;
    }

    @Override
    public boolean print(PrintStream out) {
        printSubTitle(out, pos, "(PKWARE) encryption header", block);
        return new ByteArrayHexView(block.getData(), offs, columnWidth).print(out);
    }
}
