package ru.olegcherednik.zip4jvm.view;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 11.11.2019
 */
final class EmptyView implements IView {

    public static final EmptyView INSTANCE = new EmptyView();

    @Override
    public boolean print(PrintStream out) {
        return false;
    }
}
