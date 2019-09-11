package ru.olegcherednik.zip4jvm.tasks;

import ru.olegcherednik.zip4jvm.model.ZipModelContext;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 10.09.2019
 */
public interface Task {

    void accept(ZipModelContext context) throws IOException;

}
