package ru.olegcherednik.zip4jvm.io.out.entry;

import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.io.writers.LocalFileHeaderWriter;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.builders.LocalFileHeaderBuilder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;

import static ru.olegcherednik.zip4jvm.io.out.entry.EntryMetadataOutputStream.COMPRESSED_DATA;

/**
 * @author Oleg Cherednik
 * @since 28.10.2024
 */
@RequiredArgsConstructor
public class SequenceOutputStream extends OutputStream {

    private final OutputStream os;

    public void writeLocalFileHeader(ZipEntry zipEntry, DataOutput out) throws IOException {
        zipEntry.setLocalFileHeaderRelativeOffs(out.getRelativeOffs());
        LocalFileHeader localFileHeader = new LocalFileHeaderBuilder(zipEntry).build();
        new LocalFileHeaderWriter(localFileHeader).write(out);
        out.mark(COMPRESSED_DATA);
    }

    @Override
    public void write(int b) throws IOException {
        os.write(b);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        os.write(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        os.close();
    }
}
