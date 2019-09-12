package ru.olegcherednik.zip4jvm.tasks;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.ZipModelContext;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 12.09.2019
 */
@RequiredArgsConstructor
public class CopyExistedEntryTask implements Task {

    private final String entryName;

    @Override
    public void accept(ZipModelContext context) throws IOException {
        ZipEntry entry = context.getZipModel().getEntryByFileName(entryName);
               int a = 0;
               a++;
    }
}
