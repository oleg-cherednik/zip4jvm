package ru.olegcherednik.zip4jvm.tasks;

import ru.olegcherednik.zip4jvm.model.ZipModelContext;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 12.09.2019
 */
public class CloseZipFileTask implements Task {

    @Override
    public void accept(ZipModelContext context) throws IOException {
        context.getOut().close();
    }
}
