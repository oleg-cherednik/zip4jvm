package ru.olegcherednik.zip4jvm.view.crypto;

import ru.olegcherednik.zip4jvm.model.block.crypto.PkwareEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
public final class PkwareEncryptionHeaderView extends View {

    private final PkwareEncryptionHeaderBlock encryptionHeader;
    private final long pos;

    public PkwareEncryptionHeaderView(PkwareEncryptionHeaderBlock encryptionHeader, long pos, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.encryptionHeader = encryptionHeader;
        this.pos = pos;

        Objects.requireNonNull(encryptionHeader, "'encryptionHeader' must not be null");
    }

    @Override
    public boolean print(PrintStream out) {
        printSubTitle(out, pos, "(PKWARE) encryption header");
        printValueLocation(out, "data:", encryptionHeader.getData());
        return new ByteArrayHexView(encryptionHeader.getData().getData(), offs, columnWidth).print(out);
    }
}
