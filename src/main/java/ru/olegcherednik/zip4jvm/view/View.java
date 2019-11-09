package ru.olegcherednik.zip4jvm.view;

import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;
import java.util.Locale;

/**
 * @author Oleg Cherednik
 * @since 05.11.2019
 */
public abstract class View {

    protected final int offs;
    protected final int columnWidth;
    private final String format;

    protected View(int offs, int columnWidth) {
        this.offs = offs;
        this.columnWidth = columnWidth;
        format = "%-" + columnWidth + "s%s";
    }

    public abstract void print(PrintStream out);

    protected final void printLine(PrintStream out, String one, Title title) {
        printLine(out, one, title.getTitle());
    }

    protected void printLine(PrintStream out, String one, String two) {
        if (offs > 0)
            one = StringUtils.repeat(" ", offs) + one;

        out.format(Locale.US, format, one, two);
        out.println();
    }

}
