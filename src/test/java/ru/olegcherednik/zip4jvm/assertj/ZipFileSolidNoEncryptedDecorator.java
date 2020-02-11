package ru.olegcherednik.zip4jvm.assertj;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.archivers.zip.ZipMethod;
import org.apache.commons.io.IOUtils;
import org.tukaani.xz.LZMAInputStream;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.zip.ZipEntry;

/**
 * @author Oleg Cherednik
 * @since 03.10.2019
 */
@SuppressWarnings("MagicConstant")
class ZipFileSolidNoEncryptedDecorator extends ZipFileDecorator {

    public ZipFileSolidNoEncryptedDecorator(Path zip) {
        super(zip);
    }

    @Override
    public InputStream getInputStream(ZipEntry entry) {
        try {
            return new InputStream() {
                private final ZipFile zipFile = new ZipFile(zip.toFile());
                private final InputStream delegate;

                {
                    ZipArchiveEntry zipEntry = zipFile.getEntry(entry.getName());

                    if (zipFile.canReadEntryData(zipEntry))
                        delegate = zipFile.getInputStream(zipEntry);
                    else if (zipEntry.getMethod() == ZipMethod.LZMA.getCode()) {
                        try (InputStream in = zipFile.getRawInputStream(zipEntry)) {
                            ByteBuffer buffer = ByteBuffer.wrap(IOUtils.readFully(in, 9)).order(ByteOrder.LITTLE_ENDIAN);

                            // Lzma sdk version used to compress this data
                            int majorVersion = buffer.get();
                            int minorVersion = buffer.get();

                            // Byte count of the following data represent as an unsigned short.
                            // Should be = 5 (propByte + dictSize) in all versions
                            int size = buffer.getShort() & 0xFFFF;

                            if (size != 5)
                                throw new UnsupportedOperationException("ZipEntry LZMA should have size 5 in header");

                            byte propByte = buffer.get();

                            // Dictionary size is an unsigned 32-bit little endian integer.
                            int dictSize = buffer.getInt();
//                            long uncompressedSize;
//                            if ((ze.getRawFlag() & (1 << 1)) != 0) {
//                                // If the entry uses EOS marker, use -1 to indicate
//                                uncompressedSize = -1;
//                            } else {
//                                uncompressedSize = ze.getSize();
//                            }

                            delegate = new LZMAInputStream(in, -1, propByte, dictSize);
                        }
                    } else
                        throw new UnsupportedOperationException("ZipEntry data can't be read: " + zipEntry.getName());
                }

                @Override
                public int read() throws IOException {
                    return delegate.read();
                }

                @Override
                public void close() throws IOException {
                    delegate.close();
                    zipFile.close();
                }

            };
        } catch(Zip4jvmException e) {
            throw e;
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }

}
