package ru.olegcherednik.zip4jvm.view;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.io.PrintStream;
import java.util.Locale;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 11.09.2025
 */
@RequiredArgsConstructor
public class PrintStreamDecorator implements Closeable {

    private final PrintStream out;

    public void format(Locale locale, String format, Object one, Object two) {
        out.format(Locale.US, format, one, two);
    }

    public void println() {
        out.println();
    }

    public void print(char ch) {
        out.print(ch);
    }

    public void println(Object obj) {
        out.println(obj);
    }

    public void println(String str, char underscore) {
        out.println(str);

        for (int i = 0; i < str.length(); i++)
            out.print(underscore);

        out.println();
    }

    public void printLine(String format, Object one, Object two, int offs) {
        one = Optional.ofNullable(one).orElse("");
        two = Optional.ofNullable(two).orElse("");

        if (offs > 0)
            one = StringUtils.repeat(" ", offs) + one;

        out.format(Locale.US, format, one, two);
        out.println();
    }

    public void printLine(Object one, int offs) {
        one = Optional.ofNullable(one).orElse("");

        if (offs > 0)
            one = StringUtils.repeat(" ", offs) + one;

        out.println(one);
    }

    // ---------- Closeable ----------

    @Override
    public void close() {
        out.close();
    }
}
