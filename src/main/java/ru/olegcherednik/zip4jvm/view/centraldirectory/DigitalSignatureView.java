package ru.olegcherednik.zip4jvm.view.centraldirectory;

import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;

import java.io.PrintStream;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 18.10.2019
 */
public final class DigitalSignatureView extends BaseView {

    private final CentralDirectory.DigitalSignature digitalSignature;
    private final Block block;

    public DigitalSignatureView(CentralDirectory.DigitalSignature digitalSignature, Block block, int offs, int columnWidth, long totalDisks) {
        super(offs, columnWidth, totalDisks);
        this.digitalSignature = requireNotNull(digitalSignature, "DigitalSignatureView.centralDirectory");
        this.block = requireNotNull(block, "DigitalSignatureView.block");
    }

    @Override
    public boolean print(PrintStream out) {
        printTitle(out, CentralDirectory.DigitalSignature.SIGNATURE, "Digital signature", block);
        return new ByteArrayHexView(digitalSignature.getSignatureData(), offs, columnWidth).print(out);
    }
}
