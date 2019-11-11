package ru.olegcherednik.zip4jvm.view;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 11.11.2019
 */
public interface IView {

    IView NULL = EmptyView.INSTANCE;

    boolean print(PrintStream out);

    default boolean print(PrintStream out, boolean emptyLine) {
        if (emptyLine)
            out.println();
        return print(out);
    }

}
