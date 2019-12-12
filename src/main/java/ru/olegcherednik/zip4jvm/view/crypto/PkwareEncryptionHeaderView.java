package ru.olegcherednik.zip4jvm.view.crypto;

import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.crypto.PkwareEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
public final class PkwareEncryptionHeaderView extends View {

    private final PkwareEncryptionHeaderBlock block;
    private final Function<Block, byte[]> getDataFunc;
    private final long pos;

    public PkwareEncryptionHeaderView(PkwareEncryptionHeaderBlock block, Function<Block, byte[]> getDataFunc, long pos, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.block = block;
        this.getDataFunc = getDataFunc;
        this.pos = pos;

        Objects.requireNonNull(block, "'encryptionHeader' must not be null");
    }

    @Override
    public boolean print(PrintStream out) {
        printSubTitle(out, pos, "(PKWARE) encryption header");
        printValueLocation(out, "data:", block.getHeader());
        return new ByteArrayHexView(getDataFunc.apply(block.getHeader()), offs, columnWidth).print(out);
    }
}
