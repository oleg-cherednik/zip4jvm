package ru.olegcherednik.zip4jvm.assertj;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Oleg Cherednik
 * @since 03.10.2019
 */
class ZipFileNoSplitNoEncryptedDecorator extends ZipFileDecorator {

    public ZipFileNoSplitNoEncryptedDecorator(Path zip) {
        super(zip);
    }

    @Override
    public InputStream getInputStream(ZipEntry entry) {
        try {
            return new InputStream() {
                private final ZipFile zipFile = new ZipFile(zip.toFile());
                private final InputStream delegate = zipFile.getInputStream(entry);

                @Override
                public int available() throws IOException {
                    return delegate.available();
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
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }

}
