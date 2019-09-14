package ru.olegcherednik.zip4jvm.tasks;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.entry.EntryOutputStream;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntryBuilder;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 10.09.2019
 */
@RequiredArgsConstructor
public final class AddEntryTask implements Task {

    private final Path path;
    private final String fileName;
    private final ZipEntrySettings entrySettings;
    private final ZipModel destZipModel;

    @Override
    public void accept(ZipModelContext context) throws IOException {
        DataOutput out = context.getOut();
        ZipEntry entry = ZipEntryBuilder.create(path, fileName, entrySettings);

        try (InputStream in = entry.getIn(); OutputStream os = EntryOutputStream.create(entry, destZipModel, out)) {
            IOUtils.copyLarge(in, os);
        }
    }

    @Override
    public String toString() {
        return '+' + fileName;
    }
}
