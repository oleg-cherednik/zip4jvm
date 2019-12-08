package ru.olegcherednik.zip4jvm.view.crypto;

import ru.olegcherednik.zip4jvm.io.readers.block.aes.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.io.readers.block.pkware.PkwareEncryptionHeader;
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
        if (encryptionHeader == null)
            return false;

        if (encryptionHeader instanceof AesEncryptionHeaderBlock)
            createView((AesEncryptionHeaderBlock)encryptionHeader).print(out);
        else if (encryptionHeader instanceof PkwareEncryptionHeader)
            createView((PkwareEncryptionHeader)encryptionHeader).print(out);
        // TODO add for unknown encryption header

        return true;
    }

    public BlockAesEncryptionHeaderView createView(AesEncryptionHeaderBlock encryptionHeader) {
        return BlockAesEncryptionHeaderView.builder()
                                           .encryptionHeader(encryptionHeader)
                                           .pos(pos)
                                           .offs(offs)
                                           .columnWidth(columnWidth).build();
    }

    public PkwareEncryptionHeaderView createView(PkwareEncryptionHeader encryptionHeader) {
        return PkwareEncryptionHeaderView.builder()
                                         .encryptionHeader(encryptionHeader)
                                         .pos(pos)
                                         .offs(offs)
                                         .columnWidth(columnWidth).build();
    }
}
