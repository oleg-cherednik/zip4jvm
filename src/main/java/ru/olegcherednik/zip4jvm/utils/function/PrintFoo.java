package ru.olegcherednik.zip4jvm.utils.function;

import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 02.12.2019
 */
public interface PrintFoo<R extends ExtraField.Record, V extends View> {

    void print(R record, V view, PrintStream out);

}
