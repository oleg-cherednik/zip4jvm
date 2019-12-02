package ru.olegcherednik.zip4jvm.view;

import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import ru.olegcherednik.zip4jvm.model.block.Block;

import java.io.PrintStream;
import java.util.Locale;
import java.util.stream.IntStream;

/**
 * @author Oleg Cherednik
 * @since 05.11.2019
 */
@Getter
public abstract class View implements IView {

    protected final int offs;
    protected final int columnWidth;
    protected final String format;
    protected final String prefix;

    protected View(int offs, int columnWidth) {
        this.offs = offs;
        this.columnWidth = columnWidth;
        format = "%-" + columnWidth + "s%s";
        prefix = StringUtils.repeat(" ", offs);
    }

    public final void printLine(PrintStream out, Object one, Object two) {
        if (offs > 0)
            one = prefix + one;

        out.format(Locale.US, format, one, two);
        out.println();
    }

    public void printLine(PrintStream out, String one) {
        if (offs > 0)
            one = prefix + one;
        out.println(one);
    }

    public void printTitle(PrintStream out, String str) {
        out.println(str);
        IntStream.range(0, str.length()).forEach(i -> out.print('='));
        out.println();
    }

    public void printTitle(PrintStream out, int signature, String title) {
        printTitle(out, String.format("(%s) %s", ViewUtils.signature(signature), title));
    }

    public void printTitle(PrintStream out, int signature, String title, Block block) {
        printTitle(out, String.format("(%s) %s", ViewUtils.signature(signature), title));
        printLocationAndSize(out, block);
    }

    public void printSubTitle(PrintStream out, int signature, long pos, String title, Block block) {
        String str = String.format("#%d (%s) %s", pos + 1, ViewUtils.signature(signature), title);
        out.println(str);
        IntStream.range(0, str.length()).forEach(i -> out.print('-'));
        out.println();
        printLocationAndSize(out, block);
    }

    public void printSubTitle(PrintStream out, long pos, String title) {
        String str = String.format("#%d %s", pos + 1, title);
        out.println(str);
        IntStream.range(0, str.length()).forEach(i -> out.print('-'));
        out.println();
    }

    public void printLocationAndSize(PrintStream out, Block block) {
        printLine(out, "- location:", String.format("%1$d (0x%1$08X) bytes", block.getOffs()));
        printLine(out, "- size:", String.format("%s bytes", block.getSize()));
    }

    public final void printValueLocation(PrintStream out, String valueName, Block block) {
        printLine(out, valueName, String.format("%1$d (0x%1$08X) bytes", block.getOffs()));
        printLine(out, "  - size:", String.format("%s bytes", block.getSize()));
    }

}
