package ru.olegcherednik.zip4jvm.view;

import org.apache.commons.lang.StringUtils;
import ru.olegcherednik.zip4jvm.model.block.Block;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.stream.IntStream;

/**
 * @author Oleg Cherednik
 * @since 05.11.2019
 */
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

    protected final void printLine(PrintStream out, String one, String two) {
        if (offs > 0)
            one = prefix + one;

        out.format(Locale.US, format, one, two);
        out.println();
    }

    protected void printLine(PrintStream out, String one) {
        if (offs > 0)
            one = prefix + one;
        out.println(one);
    }

    protected void printTitle(PrintStream out, String str) {
        out.println(str);
        IntStream.range(0, str.length()).forEach(i -> out.print('='));
        out.println();
    }

    protected void printTitle(PrintStream out, int signature, String title) {
        printTitle(out, String.format("(%s) %s", ViewUtils.signature(signature), title));
    }

    protected void printTitle(PrintStream out, int signature, String title, Block block) {
        printTitle(out, String.format("(%s) %s", ViewUtils.signature(signature), title));
        printLocationAndSize(out, block);
    }

    protected void printSubTitle(PrintStream out, int signature, long pos, String title, Block block) {
        String str = String.format("#%d (%s) %s", pos + 1, ViewUtils.signature(signature), title);
        out.println(str);
        IntStream.range(0, str.length()).forEach(i -> out.print('-'));
        out.println();
        printLocationAndSize(out, block);
    }

    protected void printSubTitle(PrintStream out, long pos, Charset charset, String title, Block block) {
        String str = String.format("#%d (%s) %s", pos + 1, charset.name(), title);
        out.println(str);
        IntStream.range(0, str.length()).forEach(i -> out.print('-'));
        out.println();
        printLocationAndSize(out, block);
    }

    protected void printSubTitle(PrintStream out, long pos, String title) {
        String str = String.format("#%d %s", pos + 1, title);
        out.println(str);
        IntStream.range(0, str.length()).forEach(i -> out.print('-'));
        out.println();
    }

    protected void printLocationAndSize(PrintStream out, Block block) {
        printLine(out, "- location:", String.format("%1$d (0x%1$08X) bytes", block.getOffs()));
        printLine(out, "- size:", String.format("%s bytes", block.getSize()));
    }

    protected final void printValueLocation(PrintStream out, String valueName, Block block) {
        printLine(out, valueName, String.format("%1$d (0x%1$08X) bytes", block.getOffs()));
        printLine(out, "  - size:", String.format("%s bytes", block.getSize()));
    }

}
