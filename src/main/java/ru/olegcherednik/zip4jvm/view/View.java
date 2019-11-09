package ru.olegcherednik.zip4jvm.view;

import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;
import java.util.Locale;
import java.util.stream.IntStream;

/**
 * @author Oleg Cherednik
 * @since 05.11.2019
 */
public abstract class View {

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

    public abstract void print(PrintStream out);

    protected final void printLine(PrintStream out, String one, String two) {
        if (offs > 0)
            one = prefix + one;

        out.format(Locale.US, format, one, two);
        out.println();
    }

    protected void printLine(PrintStream out, String one) {
        if (offs > 0)
            one = StringUtils.repeat(" ", offs) + one;
        out.println(one);
    }

    protected void printTitle(PrintStream out, String str) {
        out.println(str);
        IntStream.range(0, str.length()).forEach(i -> out.print('='));
    }

}
