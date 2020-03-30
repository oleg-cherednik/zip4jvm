package ru.olegcherednik.zip4jvm.view.crypto;

import ru.olegcherednik.zip4jvm.view.BaseView;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 19.03.2020
 */
public class RecipientView extends BaseView {

    protected RecipientView(int offs, int columnWidth) {
        super(offs, columnWidth);
    }

    @Override
    public boolean print(PrintStream out) {
        return false;
    }
}
