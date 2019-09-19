package ru.olegcherednik.zip4jvm.model.entry.v2;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.utils.function.IOSupplier2;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 19.09.2019
 */
@Getter
@RequiredArgsConstructor
public final class RegularFileZipEntrySource {

    private final IOSupplier2<InputStream> inputStream;
    private final String fileName;
    private final long lastModifiedTime;
    private final ExternalFileAttributes externalFileAttributes;
    private final long uncompressedSize;

    public static RegularFileZipEntrySource of(@NonNull Path path, @NonNull String fileName) throws IOException {
        if (Files.isRegularFile(path)) {
            IOSupplier2<InputStream> inputStream = () -> new FileInputStream(path.toFile());
            long lastModifiedTime = Files.getLastModifiedTime(path).toMillis();
            ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.createOperationBasedDelegate(path);
            long uncompressedSize = Files.size(path);
            return new RegularFileZipEntrySource(inputStream, fileName, lastModifiedTime, externalFileAttributes, uncompressedSize);
        }

        throw new Zip4jException("Cannot create source for directory");
    }

}
