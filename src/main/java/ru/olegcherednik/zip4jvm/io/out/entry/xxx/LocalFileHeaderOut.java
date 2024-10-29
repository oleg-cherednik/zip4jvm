package ru.olegcherednik.zip4jvm.io.out.entry.xxx;

import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.io.writers.LocalFileHeaderWriter;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.builders.LocalFileHeaderBuilder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 28.10.2024
 */
public final class LocalFileHeaderOut {

    public void write(ZipEntry zipEntry, DataOutput out) throws IOException {
        zipEntry.setLocalFileHeaderRelativeOffs(out.getRelativeOffs());
        LocalFileHeader localFileHeader = new LocalFileHeaderBuilder(zipEntry).build();
        new LocalFileHeaderWriter(localFileHeader).write(out);
    }

}
