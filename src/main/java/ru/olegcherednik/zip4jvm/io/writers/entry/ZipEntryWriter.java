package ru.olegcherednik.zip4jvm.io.writers.entry;

import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.data.EncryptedDataOutput;
import ru.olegcherednik.zip4jvm.io.out.entry.PayloadCalculationOutputStream;
import ru.olegcherednik.zip4jvm.io.out.entry.compressed.CompressedEntryDataOutput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

/**
 * @author Oleg Cherednik
 * @since 06.11.2024
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@SuppressWarnings("PMD.CloseResource")
public abstract class ZipEntryWriter implements Writer {

    private static final String COMPRESSED_DATA =
            ZipEntryWriter.class.getSimpleName() + ".entryCompressedDataOffs";

    protected final ZipEntry zipEntry;

    public static ZipEntryWriter create(ZipEntry entry, Path tempDir) {
        if (entry.isDataDescriptorAvailable())
            return new ZipEntryDataDescriptorWriter(entry);

        Path dir = tempDir.resolve(UUID.randomUUID().toString());
        return new ZipEntryNoDataDescriptorWriter(entry, dir);
    }

//    @Override
//    public void write(DataOutput out) throws IOException {
//        // 1. compression
//        // 2. encryption
//        zipEntry.setDiskNo(out.getDiskNo());
//
//        /*
//        The series of
//        [local file header]
//        [encryption header]
//        [file data]
//        [data descriptor]
//         */
//
//        new LocalFileHeaderOut().write(zipEntry, out);
//        foo(out);
//        new UpdateZip64().update(zipEntry);
//        new DataDescriptorOut().write(zipEntry, out);
//    }

    protected void foo(DataOutput out) throws IOException {
        out.mark(COMPRESSED_DATA);

        EncryptedDataOutput edo = EncryptedDataOutput.create(zipEntry, out);
        DataOutput cos = CompressedEntryDataOutput.create(zipEntry, edo);

        try (InputStream in = zipEntry.getInputStream();
             PayloadCalculationOutputStream os = new PayloadCalculationOutputStream(zipEntry, cos)) {
            IOUtils.copyLarge(in, os);
        }

        edo.encodingAccomplished();
        zipEntry.setCompressedSize(out.getWrittenBytesAmount(COMPRESSED_DATA));
    }

    @Override
    public String toString() {
        return '+' + zipEntry.getFileName();
    }

}
