package ru.olegcherednik.zip4jvm.io.out.entry;

import lombok.NonNull;
import ru.olegcherednik.zip4jvm.crypto.Encoder;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
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

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
public abstract class EntryOutputStream extends OutputStream {

    private static final String COMPRESSED_DATA = "entryCompressedDataOffs";

    private final ZipEntry entry;
    private final Checksum checksum = new CRC32();

    protected final Encoder encoder;
    protected final DataOutput out;

    public static EntryOutputStream create(@NonNull ZipEntry entry, @NonNull ZipModel zipModel, @NonNull DataOutput out) throws IOException {
        EntryOutputStream os = createOutputStream(entry, entry.getCompression(), out);

        // TODO move it to the separate method
        zipModel.addEntry(entry);
        entry.setLocalFileHeaderOffs(out.getOffs());

        os.writeLocalFileHeader();
        os.writeEncryptionHeader();
        return os;
    }

    public static EntryOutputStream create(@NonNull ZipEntry entry, @NonNull Compression compression, @NonNull DataOutput out) throws IOException {
        EntryOutputStream os = createOutputStream(entry, compression, out);

        entry.setLocalFileHeaderOffs(out.getOffs());

        os.writeLocalFileHeader();
        os.writeEncryptionHeader();
        return os;
    }

    private static EntryOutputStream createOutputStream(ZipEntry entry, Compression compression, DataOutput out) throws IOException {
        entry.setDisk(out.getDisk());

        if (compression == Compression.STORE)
            return new StoreEntryOutputStream(entry, out);
        if (compression == Compression.DEFLATE)
            return new DeflateEntryOutputStream(entry, out);

        throw new Zip4jException("Compression is not supported: " + compression);
    }

    protected EntryOutputStream(ZipEntry entry, DataOutput out) {
        this.entry = entry;
        this.out = out;
        encoder = entry.getEncryption().getCreateEncoder().apply(entry);
    }

    private void writeLocalFileHeader() throws IOException {
        LocalFileHeader localFileHeader = new LocalFileHeaderBuilder(entry).create();
        new LocalFileHeaderWriter(localFileHeader).write(out);
        out.mark(COMPRESSED_DATA);
    }

    private void writeEncryptionHeader() throws IOException {
        encoder.writeEncryptionHeader(out);
    }

    protected final void updateChecksum(byte[] buf, int offs, int len) {
        checksum.update(buf, offs, len);
    }

    @Override
    public final void write(int b) throws IOException {
        write(new byte[] { (byte)b }, 0, 1);
    }

    @Override
    public void close() throws IOException {
        encoder.close(out);
        entry.setChecksum(checksum.getValue());
        entry.checkCompressedSize(out.getWrittenBytesAmount(COMPRESSED_DATA));
        entry.setCompressedSize(out.getWrittenBytesAmount(COMPRESSED_DATA));
        writeDataDescriptor();
    }

    private void writeDataDescriptor() throws IOException {
        if (!entry.isDataDescriptorAvailable())
            return;

        DataDescriptor dataDescriptor = new DataDescriptor();
        dataDescriptor.setCrc32(checksum.getValue());
        dataDescriptor.setCompressedSize(entry.getCompressedSize());
        dataDescriptor.setUncompressedSize(entry.getUncompressedSize());
        DataDescriptorWriter.get(entry.isZip64(), dataDescriptor).write(out);
    }

}
