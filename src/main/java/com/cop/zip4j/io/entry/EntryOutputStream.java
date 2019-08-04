package com.cop.zip4j.io.entry;

import com.cop.zip4j.core.writers.DataDescriptorWriter;
import com.cop.zip4j.core.writers.LocalFileHeaderWriter;
import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.CentralDirectoryBuilder;
import com.cop.zip4j.io.out.MarkDataOutput;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.DataDescriptor;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.entry.PathZipEntry;
import com.cop.zip4j.utils.InternalZipConstants;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@RequiredArgsConstructor
public abstract class EntryOutputStream extends OutputStream {

    private static final String MARK = "entry";

    @NonNull
    private final ZipModel zipModel;
    protected final MarkDataOutput out;
    protected final Checksum checksum = new CRC32();

    private CentralDirectory.FileHeader fileHeader;
    private LocalFileHeader localFileHeader;

    @NonNull
    protected Encoder encoder = Encoder.NULL;

    public static EntryOutputStream create(@NonNull PathZipEntry entry, @NonNull ZipModel zipModel, @NonNull MarkDataOutput out) throws IOException {
        EntryOutputStream res = createOutputStream(entry, zipModel, out);
        res.putNextEntry(entry);
        return res;
    }

    private static EntryOutputStream createOutputStream(@NonNull PathZipEntry entry, @NonNull ZipModel zipModel, @NonNull MarkDataOutput out) {
        Compression compression = entry.getCompression();

        if (compression == Compression.DEFLATE)
            return new DeflateEntryOutputStream(zipModel, out, entry.getCompressionLevel());
        if (compression == Compression.STORE)
            return new StoreEntryOutputStream(zipModel, out);

        throw new Zip4jException("Compression is not supported: " + compression);
    }

    private void putNextEntry(PathZipEntry entry) throws IOException {
        CentralDirectoryBuilder centralDirectoryBuilder = new CentralDirectoryBuilder(entry, zipModel, out.getCounter());

        fileHeader = centralDirectoryBuilder.createFileHeader();
        localFileHeader = centralDirectoryBuilder.createLocalFileHeader(fileHeader);

        if (zipModel.isSplitArchive() && zipModel.isEmpty())
            out.writeDword(InternalZipConstants.SPLITSIG);

        fileHeader.setOffsLocalFileHeader(out.getOffs());
        new LocalFileHeaderWriter(zipModel, localFileHeader).write(out);

        encoder = entry.getEncryption().encoder(localFileHeader, entry);

        out.mark(MARK);
        encoder.writeHeader(out);
    }

    @Override
    public final void write(int b) throws IOException {
        write(new byte[] { (byte)b }, 0, 1);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        checksum.update(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        encoder.close(out);

        fileHeader.setCrc32(fileHeader.getEncryption() == Encryption.AES ? 0 : checksum.getValue());
        fileHeader.setCompressedSize(out.getWrittenBytesAmount(MARK));
        zipModel.addFileHeader(fileHeader);

        writeDataDescriptor();

        out.mark(MARK);
    }

    private void writeDataDescriptor() throws IOException {
        // TODO should be isDataDescriptorExists == true only when parameters.getCompressionMethod() == CompressionMethod.DEFLATE
        if (!localFileHeader.getGeneralPurposeFlag().isDataDescriptorExists())
            return;

        DataDescriptor dataDescriptor = new DataDescriptor();
        dataDescriptor.setCrc32(fileHeader.getCrc32());
        dataDescriptor.setCompressedSize(fileHeader.getCompressedSize());
        dataDescriptor.setUncompressedSize(fileHeader.getUncompressedSize());

        new DataDescriptorWriter(dataDescriptor).write(out);
    }

}
