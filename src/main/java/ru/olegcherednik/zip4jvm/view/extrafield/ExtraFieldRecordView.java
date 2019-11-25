package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 24.11.2019
 */
public abstract class ExtraFieldRecordView extends View {

    protected final Block block;

    protected ExtraFieldRecordView(Block block, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.block = block;
    }

    protected abstract int getSignature();

    protected abstract String getTitle();

    public String getFileName() {
        String title = getTitle();
        title = title.replaceAll(" ", "_");
        title = title.replaceAll("[/\\\\]", "-");
        return String.format("(0x%04X)_%s", getSignature(), title);
    }

    @Override
    public boolean print(PrintStream out) {
        printValueLocation(out, String.format("(0x%04X) %s:", getSignature(), getTitle()), block);
        return true;
    }
}
