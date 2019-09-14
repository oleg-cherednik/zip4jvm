package ru.olegcherednik.zip4jvm.io.writers;

import lombok.NonNull;
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
public final class RegularFileWriter implements Writer {

    private final Path path;
    private final String fileName;
    private final ZipEntrySettings entrySettings;
    private final ZipModel tempZipModel;

    @Override
    public void write(@NonNull DataOutput out) throws IOException {
        ZipEntry entry = ZipEntryBuilder.create(path, fileName, entrySettings);

        try (InputStream in = entry.getIn(); OutputStream os = EntryOutputStream.create(entry, tempZipModel, out)) {
            IOUtils.copyLarge(in, os);
        }
    }

    @Override
    public String toString() {
        return '+' + fileName;
    }
}
