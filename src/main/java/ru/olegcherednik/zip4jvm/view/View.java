package ru.olegcherednik.zip4jvm.view;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;
import java.util.Locale;

/**
 * @author Oleg Cherednik
 * @since 05.11.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class View {

    private final int offs;
    private final int secondColumnPos;

    public abstract void print(PrintStream out);

    protected final void printLine(PrintStream out, String one, Title title) {
        printLine(out, one, title.getTitle());
    }

    protected void printLine(PrintStream out, String one, String two) {
        if (offs > 0)
            one = StringUtils.repeat(" ", offs) + one;
        out.format(Locale.US, "%-" + secondColumnPos + "s%s\n", one, two);
    }

}
