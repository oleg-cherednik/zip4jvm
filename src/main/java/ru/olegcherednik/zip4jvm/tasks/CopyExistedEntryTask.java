package ru.olegcherednik.zip4jvm.tasks;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.io.in.SplitZipInputStream;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.io.readers.DataDescriptorReader;
import ru.olegcherednik.zip4jvm.io.readers.LocalFileHeaderReader;
import ru.olegcherednik.zip4jvm.io.writers.DataDescriptorWriter;
import ru.olegcherednik.zip4jvm.io.writers.LocalFileHeaderWriter;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.ZipModelContext;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 12.09.2019
 */
@RequiredArgsConstructor
public class CopyExistedEntryTask implements Task {

    private final String entryName;

    private static final String LOCAL_FILE_HEADER_OFFS = "localFileHeaderOffs";

    @Override
    public void accept(ZipModelContext context) throws IOException {
        ZipEntry entry = context.getZipModel().getEntryByFileName(entryName);
        DataOutput out = context.getOut();
        ZipModel zipModel = context.getZipModel();

        try (CopyEntryInputStream in = new CopyEntryInputStream(entry, zipModel)) {
            out.mark(LOCAL_FILE_HEADER_OFFS);

            in.copyLocalFileHeader(out);
            in.copyEncryptionHeaderAndData(out);
            in.copyDataDescriptor(out);

            entry.setLocalFileHeaderOffs(out.getMark(LOCAL_FILE_HEADER_OFFS));
        }
    }

    @Override
    public String toString() {
        return "->" + entryName;
    }


    private static final class CopyEntryInputStream implements Closeable {

        private final ZipEntry entry;
        private final DataInput in;
        private final Decoder decoder;

        public CopyEntryInputStream(ZipEntry entry, ZipModel zipModel) throws IOException {
            this.entry = entry;
            in = zipModel.isSplit() ? SplitZipInputStream.create(zipModel, entry.getDisk()) : SingleZipInputStream.create(zipModel);
            decoder = entry.getEncryption().getCreateDecoder().apply(entry, in);
        }

        public void copyLocalFileHeader(@NonNull DataOutput out) throws IOException {
            LocalFileHeader localFileHeader = new LocalFileHeaderReader(entry.getLocalFileHeaderOffs()).read(in);
            entry.setDataDescriptorAvailable(() -> localFileHeader.getGeneralPurposeFlag().isDataDescriptorAvailable());
            new LocalFileHeaderWriter(localFileHeader).write(out);
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
}
