package ru.olegcherednik.zip4jvm.tasks;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.entry.EntryOutputStream;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.ZipModelContext;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 10.09.2019
 */
@RequiredArgsConstructor
public final class AddEntryTask implements Task {

    private final ZipModel zipModel;
    private final ZipEntry entry;

    @Override
    public void accept(ZipModelContext context) throws IOException {
        DataOutput out = context.getOut();

        try (InputStream in = entry.getIn(); OutputStream os = EntryOutputStream.create(entry, zipModel, out)) {
            IOUtils.copyLarge(in, os);
        }
    }

    @Override
    public String toString() {
        return '+' + entry.getFileName();
    }
}
