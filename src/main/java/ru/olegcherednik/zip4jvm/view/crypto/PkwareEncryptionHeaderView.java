package ru.olegcherednik.zip4jvm.view.crypto;

import ru.olegcherednik.zip4jvm.model.block.crypto.PkwareEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
final class PkwareEncryptionHeaderView extends View {

    private final PkwareEncryptionHeaderBlock block;
    private final long pos;

    public PkwareEncryptionHeaderView(PkwareEncryptionHeaderBlock block, long pos, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.block = block;
        this.pos = pos;

        requireNotNull(block, "PkwareEncryptionHeaderView.centralDirectory");
    }

    @Override
    public boolean print(PrintStream out) {
        printSubTitle(out, pos, "(PKWARE) encryption header");
        printValueLocation(out, "data:", block);
        return new ByteArrayHexView(block.getData(), offs, columnWidth).print(out);
    }
}
