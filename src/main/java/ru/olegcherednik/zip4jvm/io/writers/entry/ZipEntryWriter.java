package ru.olegcherednik.zip4jvm.io.writers.entry;

import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.data.EncryptedDataOutput;
import ru.olegcherednik.zip4jvm.io.out.entry.PayloadCalculationOutputStream;
import ru.olegcherednik.zip4jvm.io.out.entry.compressed.CompressedEntryDataOutput;
import ru.olegcherednik.zip4jvm.io.writers.LocalFileHeaderWriter;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.builders.LocalFileHeaderBuilder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_ENTRY_SIZE;
import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_LOCAL_FILE_HEADER_OFFS;
import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_TOTAL_DISKS;

/**
 * @author Oleg Cherednik
 * @since 06.11.2024
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@SuppressWarnings("PMD.CloseResource")
public class ZipEntryWriter implements Writer {

    private static final String COMPRESSED_DATA =
            ZipEntryWriter.class.getSimpleName() + ".entryCompressedDataOffs";

    protected final ZipEntry zipEntry;

    public static ZipEntryWriter create(ZipEntry entry, Path tempDir) {
        if (entry.isDataDescriptorAvailable())
            return new ZipEntryWithDataDescriptorWriter(entry);

        Path dir = tempDir.resolve(UUID.randomUUID().toString());
        return new ZipEntryWithoutDataDescriptorWriter(entry, dir);
    }

    protected void writeLocalFileHeader(DataOutput out) throws IOException {
        zipEntry.setLocalFileHeaderRelativeOffs(out.getDiskOffs());
        LocalFileHeader localFileHeader = new LocalFileHeaderBuilder(zipEntry).build();
        new LocalFileHeaderWriter(localFileHeader).write(out);
    }

    protected void updateZip64() {
        if (zipEntry.getCompressedSize() > MAX_ENTRY_SIZE)
            zipEntry.setZip64(true);
        if (zipEntry.getUncompressedSize() > MAX_ENTRY_SIZE)
            zipEntry.setZip64(true);
        if (zipEntry.getDiskNo() > MAX_TOTAL_DISKS)
            zipEntry.setZip64(true);
        if (zipEntry.getLocalFileHeaderRelativeOffs() > MAX_LOCAL_FILE_HEADER_OFFS)
            zipEntry.setZip64(true);
    }

    protected void writePayload(DataOutput out) throws IOException {
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

    // ---------- Writer ----------

    @Override
    public void write(DataOutput out) throws IOException {
        zipEntry.setDiskNo(out.getDiskNo());
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return '+' + zipEntry.getFileName();
    }

}
