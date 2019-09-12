package ru.olegcherednik.zip4jvm.tasks;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.ZipModelContext;

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
               int a = 0;
               a++;
    }
}
