package ru.olegcherednik.zip4jvm.view.centraldirectory;

import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;

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
    }

    @Override
    public boolean print(PrintStream out) {
        if (digitalSignature == null || ArrayUtils.isEmpty(digitalSignature.getSignatureData()))
            return false;

        printTitle(out, CentralDirectory.DigitalSignature.SIGNATURE, "Digital signature", block);
        printData(out);

        return true;
    }

    private boolean printData(PrintStream out) {
        return ByteArrayHexView.builder()
                               .data(digitalSignature.getSignatureData())
                               .position(offs, columnWidth).build().print(out);
    }
}
