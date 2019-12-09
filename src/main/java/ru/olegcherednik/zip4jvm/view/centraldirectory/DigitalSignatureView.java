package ru.olegcherednik.zip4jvm.view.centraldirectory;

import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 18.10.2019
 */
public final class DigitalSignatureView extends View {

    private final CentralDirectory.DigitalSignature digitalSignature;
    private final Block block;

    public DigitalSignatureView(CentralDirectory.DigitalSignature digitalSignature, Block block, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.digitalSignature = digitalSignature;
        this.block = block;

        Objects.requireNonNull(digitalSignature, "'digitalSignature' must not be null");
        Objects.requireNonNull(block, "'block' must not be null");
    }

    @Override
    public boolean print(PrintStream out) {
        printTitle(out, CentralDirectory.DigitalSignature.SIGNATURE, "Digital signature", block);
        return new ByteArrayHexView(digitalSignature.getSignatureData(), offs, columnWidth).print(out);
    }
}
