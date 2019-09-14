package ru.olegcherednik.zip4jvm.io.in.entry;

import lombok.NonNull;
import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.entry.EntryOutputStream;
import ru.olegcherednik.zip4jvm.io.readers.DataDescriptorReader;
import ru.olegcherednik.zip4jvm.io.readers.LocalFileHeaderReader;
import ru.olegcherednik.zip4jvm.io.writers.DataDescriptorWriter;
import ru.olegcherednik.zip4jvm.io.writers.LocalFileHeaderWriter;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
public final class CopyEntryInputStream extends EntryInputStream {

    public CopyEntryInputStream(ZipEntry entry, DataInput in, Decoder decoder) {
        super(entry, in, decoder);
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        len = (int)Math.min(len, getAvailableCompressedBytes());
        len = in.read(buf, offs, len);

        if (len == 0 || len == IOUtils.EOF)
            return IOUtils.EOF;

        decoder.decrypt(buf, offs, len);
        readCompressedBytes += len;
        writtenUncompressedBytes += len;
        updateChecksum(buf, offs, len);
        return len;
    }

    public void copyLocalFileHeader(@NonNull DataOutput out) throws IOException {
        LocalFileHeader localFileHeader = new LocalFileHeaderReader(entry.getLocalFileHeaderOffs()).read(in);
        new LocalFileHeaderWriter(localFileHeader).write(out);
        out.mark(EntryOutputStream.COMPRESSED_DATA);
    }

    public void copyEncryptionHeaderAndData(@NonNull DataOutput out) throws IOException {
        long size = decoder.getSizeOnDisk(entry);
        byte[] buf = new byte[1024 * 4];

        while (size > 0) {
            int n = in.read(buf, 0, (int)Math.min(buf.length, size));

            if (n == IOUtils.EOF)
                throw new Zip4jException("Unexpected end of file");

            out.write(buf, 0, n);
            size -= n;
        }
    }

    public void copyDataDescriptor(@NonNull DataOutput out) throws IOException {
        if (entry.isDataDescriptorAvailable()) {
            DataDescriptor dataDescriptor = DataDescriptorReader.get(entry.isZip64()).read(in);
            DataDescriptorWriter.get(entry.isZip64(), dataDescriptor).write(out);
        }
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

}
