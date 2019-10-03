package ru.olegcherednik.zip4jvm.assertj;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * @author Oleg Cherednik
 * @since 03.10.2019
 */
@SuppressWarnings("MethodCanBeVariableArityMethod")
class ZipFileSplitDecorator extends ZipFileDecorator {

    private final char[] password;

    public ZipFileSplitDecorator(Path zip) {
        this(zip, null);
    }

    public ZipFileSplitDecorator(Path zip, char[] password) {
        super(zip, entries(zip));
        this.password = ArrayUtils.clone(password);
    }

    @Override
    public InputStream getInputStream(ZipEntry entry) {
        try {
            return new InputStream() {
                private final ZipInputStream delegate;

                {
                    ZipFile zipFile = new ZipFile(zip.toFile(), password);
                    delegate = zipFile.getInputStream(zipFile.getFileHeader(entry.getName()));
                }

                @Override
                public int available() throws IOException {
                    return (int)entries.get(entry.getName()).getSize();
                }

                @Override
                public int read() throws IOException {
                    return delegate.read();
                }

                @Override
                public void close() throws IOException {
                    delegate.close();
                }

            };
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    private static Map<String, ZipEntry> entries(Path path) {
        try {
            return new ZipFile(path.toFile()).getFileHeaders().stream()
                                             .map(fileHeader -> {
                                                 ZipEntry zipEntry = new ZipEntry(fileHeader.getFileName());
                                                 zipEntry.setSize(fileHeader.getUncompressedSize());
                                                 zipEntry.setCompressedSize(fileHeader.getCompressedSize());
                                                 zipEntry.setCrc(fileHeader.getCrc());
                                                 return zipEntry;
                                             })
                                             .collect(Collectors.toMap(ZipEntry::getName, Function.identity()));
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }

}
