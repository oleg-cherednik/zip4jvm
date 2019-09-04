package com.cop.zip4j.io.out.entry;

import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.io.writers.DataDescriptorWriter;
import com.cop.zip4j.io.writers.LocalFileHeaderWriter;
import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.DataDescriptor;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.builders.LocalFileHeaderBuilder;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.NonNull;

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

    private final PathZipEntry entry;
    private final Checksum checksum = new CRC32();

    protected final Encoder encoder;
    protected final DataOutput out;

    public static EntryOutputStream create(@NonNull PathZipEntry entry, @NonNull ZipModel zipModel, @NonNull DataOutput out) throws IOException {
        EntryOutputStream os = createOutputStream(entry, out);

        // TODO move it to the separate method
        zipModel.getEntries().add(entry);
        entry.setLocalFileHeaderOffs(out.getOffs());

        os.writeLocalFileHeader();
        os.writeEncryptionHeader();
        return os;
    }

    private static EntryOutputStream createOutputStream(PathZipEntry entry, DataOutput out) throws IOException {
        Compression compression = entry.getCompression();
        entry.setDisk(out.getDisk());

        if (compression == Compression.STORE)
            return new StoreEntryOutputStream(entry, out);
        if (compression == Compression.DEFLATE)
            return new DeflateEntryOutputStream(entry, out);

        throw new Zip4jException("Compression is not supported: " + compression);
    }

    protected EntryOutputStream(PathZipEntry entry, DataOutput out) {
        this.entry = entry;
        this.out = out;
        encoder = entry.getEncryption().getCreateEncoder().apply(entry);
    }

    private void writeLocalFileHeader() throws IOException {
        LocalFileHeader localFileHeader = new LocalFileHeaderBuilder(entry).create();
        new LocalFileHeaderWriter(localFileHeader, entry.getCharset()).write(out);
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
        // TODO merge these two methods
        entry.checkCompressedSize(out.getWrittenBytesAmount(COMPRESSED_DATA));
        entry.setCompressedSize(out.getWrittenBytesAmount(COMPRESSED_DATA));
        writeDataDescriptor();
    }

    private void writeDataDescriptor() throws IOException {
        if (entry.isDataDescriptorAvailable()) {
            DataDescriptor dataDescriptor = new DataDescriptor();
            dataDescriptor.setCrc32(checksum.getValue());
            dataDescriptor.setCompressedSize(entry.getCompressedSize());
            dataDescriptor.setUncompressedSize(entry.getUncompressedSize());
            DataDescriptorWriter.get(entry.isZip64(), dataDescriptor).write(out);
        }
    }

}
