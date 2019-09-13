package ru.olegcherednik.zip4jvm.tasks;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.entry.EntryOutputStream;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.ZipModelContext;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
        DataOutput out = context.getOut();

        try (InputStream in = entry.getIn(); OutputStream os = EntryOutputStream.create(entry, Compression.STORE, out)) {
            IOUtils.copyLarge(in, os);
        }
    }
}
