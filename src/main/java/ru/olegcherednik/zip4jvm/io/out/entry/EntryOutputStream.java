package ru.olegcherednik.zip4jvm.io.out.entry;

import ru.olegcherednik.zip4jvm.crypto.Encoder;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.io.writers.DataDescriptorWriter;
import ru.olegcherednik.zip4jvm.io.writers.LocalFileHeaderWriter;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.LocalFileHeaderBuilder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_ENTRY_SIZE;
import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_LOCAL_FILE_HEADER_OFFS;
import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_TOTAL_DISKS;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
public abstract class EntryOutputStream extends OutputStream {

    private static final String COMPRESSED_DATA = "entryCompressedDataOffs";

    private final ZipEntry zipEntry;
    private final Checksum checksum = new CRC32();

    protected final Encoder encoder;
    protected final DataOutput out;

    private long uncompressedSize;

    public static EntryOutputStream create(ZipEntry zipEntry, ZipModel zipModel, DataOutput out) throws IOException {
        EntryOutputStream os = createOutputStream(zipEntry, out);

        // TODO move it to the separate method
        zipModel.addEntry(zipEntry);
        zipEntry.setLocalFileHeaderOffs(out.getOffs());

        os.writeLocalFileHeader();
        os.writeEncryptionHeader();
        return os;
    }

    private static EntryOutputStream createOutputStream(ZipEntry zipEntry, DataOutput out) throws IOException {
        Compression compression = zipEntry.getCompression();
        zipEntry.setDisk(out.getDisk());

        if (compression == Compression.STORE)
            return new StoreEntryOutputStream(zipEntry, out);
        if (compression == Compression.DEFLATE)
            return new DeflateEntryOutputStream(zipEntry, out);

        throw new Zip4jvmException("Compression is not supported: " + compression);
    }

    protected EntryOutputStream(ZipEntry zipEntry, DataOutput out) {
        this.zipEntry = zipEntry;
        this.out = out;
        encoder = zipEntry.getEncryption().getCreateEncoder().apply(zipEntry);
    }

    private void writeLocalFileHeader() throws IOException {
        LocalFileHeader localFileHeader = new LocalFileHeaderBuilder(zipEntry).build();
        new LocalFileHeaderWriter(localFileHeader).write(out);
        out.mark(COMPRESSED_DATA);
    }

    private void writeEncryptionHeader() throws IOException {
        encoder.writeEncryptionHeader(out);
    }

    @Override
    public final void write(int b) throws IOException {
        write(new byte[] { (byte)b }, 0, 1);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        checksum.update(buf, offs, len);
        uncompressedSize += Math.max(0, len);
    }

    @Override
    public void close() throws IOException {
        encoder.close(out);
        zipEntry.setChecksum(checksum.getValue());
        zipEntry.setUncompressedSize(uncompressedSize);
        zipEntry.setCompressedSize(out.getWrittenBytesAmount(COMPRESSED_DATA));
        updateZip64();
        writeDataDescriptor();
    }

    private void updateZip64() {
        if (zipEntry.getCompressedSize() > MAX_ENTRY_SIZE)
            zipEntry.setZip64(true);
        if (zipEntry.getUncompressedSize() > MAX_ENTRY_SIZE)
            zipEntry.setZip64(true);
        if (zipEntry.getDisk() > MAX_TOTAL_DISKS)
            zipEntry.setZip64(true);
        if (zipEntry.getLocalFileHeaderOffs() > MAX_LOCAL_FILE_HEADER_OFFS)
            zipEntry.setZip64(true);
    }

    private void writeDataDescriptor() throws IOException {
        if (!zipEntry.isDataDescriptorAvailable())
            return;

        DataDescriptor dataDescriptor = new DataDescriptor();
        dataDescriptor.setCrc32(checksum.getValue());
        dataDescriptor.setCompressedSize(zipEntry.getCompressedSize());
        dataDescriptor.setUncompressedSize(zipEntry.getUncompressedSize());
        DataDescriptorWriter.get(zipEntry.isZip64(), dataDescriptor).write(out);
    }

    @Override
    public String toString() {
        return out.toString();
    }

}
