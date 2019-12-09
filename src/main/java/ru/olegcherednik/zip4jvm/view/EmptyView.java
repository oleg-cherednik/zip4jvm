package ru.olegcherednik.zip4jvm.view;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 11.11.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class EmptyView implements IView {

    public static final EmptyView INSTANCE = new EmptyView();

    @Override
    public boolean print(PrintStream out) {
        return false;
    }

    @Override
    public boolean print(PrintStream out, boolean emptyLine) {
        return false;
    }
}
