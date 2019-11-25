package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.View;

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

    public abstract int getSignature();

    public abstract String getTitle();

    public String getFileName() {
        return String.format("(0x%04X)_%s", getSignature(), getTitle());
    }
}
