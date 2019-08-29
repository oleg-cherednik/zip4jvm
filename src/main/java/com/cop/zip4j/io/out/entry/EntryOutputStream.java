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
import java.nio.charset.Charset;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
public abstract class EntryOutputStream extends OutputStream {

    public static final int SPLIT_SIGNATURE = 0x08074b50;

    private static final String COMPRESSED_DATA = EntryOutputStream.class.getSimpleName();

    private final ZipModel zipModel;
    private final PathZipEntry entry;
    private final Checksum checksum = new CRC32();

    protected final Encoder encoder;
    protected final DataOutput out;

    private boolean dataDescriptorExists;

    public static EntryOutputStream create(@NonNull PathZipEntry entry, @NonNull ZipModel zipModel, @NonNull DataOutput out) throws IOException {
        EntryOutputStream res = createOutputStream(entry, zipModel, out);
        res.writeHeader();
        return res;
    }

    private static EntryOutputStream createOutputStream(PathZipEntry entry, ZipModel zipModel, DataOutput out) throws IOException {
        Compression compression = entry.getCompression();
        entry.setDisc(out.getCounter());

        if (compression == Compression.STORE)
            return new StoreEntryOutputStream(zipModel, entry, out);
        if (compression == Compression.DEFLATE)
            return new DeflateEntryOutputStream(zipModel, entry, out);

        throw new Zip4jException("Compression is not supported: " + compression);
    }

    protected EntryOutputStream(ZipModel zipModel, PathZipEntry entry, DataOutput out) {
        this.zipModel = zipModel;
        this.entry = entry;
        this.out = out;
        encoder = entry.getEncryption().getCreateEncoder().apply(entry);
    }

    private void writeHeader() throws IOException {
        // only at the beginning of the split file
        if (zipModel.isSplitArchive() && zipModel.isEmpty())
            out.writeDwordSignature(SPLIT_SIGNATURE);

        zipModel.getEntries().add(entry);
        entry.setLocalFileHeaderOffs(out.getOffs());

        writeLocalFileHeader();
        writeEncryptionHeader();
    }

    private void writeLocalFileHeader() throws IOException {
        Charset charset = zipModel.getCharset();
        LocalFileHeader localFileHeader = new LocalFileHeaderBuilder(entry, charset).create();
        dataDescriptorExists = localFileHeader.getGeneralPurposeFlag().isDataDescriptorExists();
        new LocalFileHeaderWriter(localFileHeader, charset).write(out);
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

        checkChecksum();
        checkCompressedSize();

        entry.setCompressedSizeNew(out.getWrittenBytesAmount(COMPRESSED_DATA));

        writeDataDescriptor();
    }

    private void checkChecksum() {
        long expected = entry.checksum();
        long actual = checksum.getValue();

        if (expected != 0 && expected != actual)
            throw new Zip4jException("Checksum is not matched: " + entry.getName());
    }

    private void checkCompressedSize() {
        long expected = entry.getCompressedSize();
        long actual = out.getWrittenBytesAmount(COMPRESSED_DATA);

        if (expected != 0 && expected != actual)
            throw new Zip4jException("CompressedSize is not matched: " + entry.getName());
    }

    private void writeDataDescriptor() throws IOException {
        if (dataDescriptorExists) {
            DataDescriptor dataDescriptor = new DataDescriptor();
            dataDescriptor.setCrc32(checksum.getValue());
            dataDescriptor.setCompressedSize(entry.getCompressedSizeNew());
            dataDescriptor.setUncompressedSize(entry.size());

            new DataDescriptorWriter(dataDescriptor, entry.getActivity()).write(out);
        }
    }

}
