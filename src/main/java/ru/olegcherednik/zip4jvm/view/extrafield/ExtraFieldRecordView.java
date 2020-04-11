package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.BaseView;

import java.io.PrintStream;
import java.util.regex.Pattern;

/**
 * @author Oleg Cherednik
 * @since 24.11.2019
 */
public abstract class ExtraFieldRecordView<T extends ExtraField.Record> extends BaseView {

    private final T record;
    private final Block block;
    private final PrintConsumer<T, BaseView> printConsumer;

    protected ExtraFieldRecordView(Builder<T, ?> builder, PrintConsumer<T, BaseView> printConsumer) {
        super(builder.getOffs(), builder.getColumnWidth(), builder.getTotalDisks());
        record = builder.getRecord();
        block = builder.getBlock();
        this.printConsumer = printConsumer;
    }

    protected int getSignature() {
        return record.getSignature();
    }

    protected String getTitle() {
        return record.getTitle();
    }

    private static final Pattern SPACE = Pattern.compile(" ");
    private static final Pattern SLASH = Pattern.compile("[/\\\\]");

    public String getFileName() {
        String title = getTitle();
        title = SPACE.matcher(title).replaceAll("_");
        title = SLASH.matcher(title).replaceAll("-");
        return String.format("(0x%04X)_%s", getSignature(), title);
    }

    @Override
    public boolean print(PrintStream out) {
        if (record == null)
            return false;

        printValueWithLocation(out, String.format("(0x%04X) %s:", getSignature(), getTitle()), block);
        printConsumer.print(record, this, out);

        return true;
    }

}
