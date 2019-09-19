package ru.olegcherednik.zip4jvm.model.entry.v2;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.utils.function.IOSupplier2;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 19.09.2019
 */
@Getter
public final class ZipEntryMeta {

    private final IOSupplier2<InputStream> inputStream;
    private final String fileName;
    private final long lastModifiedTime;
    private final ExternalFileAttributes externalFileAttributes;

    public static ZipEntryMeta of(@NonNull Path path, @NonNull String fileName) throws IOException {
        if (Files.isRegularFile(path))
            return builder()
                    .inputStream(() -> new FileInputStream(path.toFile()))
                    .fileName(fileName)
                    .lastModifiedTime(Files.getLastModifiedTime(path).toMillis())
                    .externalFileAttributes(ExternalFileAttributes.createOperationBasedDelegate(path)).build();

        throw new Zip4jException("Cannot create source for directory");
    }

    public static Builder builder() {
        return new Builder();
    }

    private ZipEntryMeta(Builder builder) {
        inputStream = builder.inputStream;
        fileName = builder.fileName;
        lastModifiedTime = builder.lastModifiedTime;
        externalFileAttributes = builder.externalFileAttributes;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private IOSupplier2<InputStream> inputStream;
        private String fileName;
        private long lastModifiedTime = System.currentTimeMillis();
        private ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.NULL;

        public ZipEntryMeta build() {
            return new ZipEntryMeta(this);
        }

        public Builder inputStream(IOSupplier2<InputStream> inputStream) {
            this.inputStream = Optional.ofNullable(inputStream).orElseGet(() -> () -> null);
            return this;
        }

        public Builder fileName(@NonNull String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder lastModifiedTime(long lastModifiedTime) {
            this.lastModifiedTime = lastModifiedTime;
            return this;
        }

        public Builder externalFileAttributes(@NonNull ExternalFileAttributes externalFileAttributes) {
            this.externalFileAttributes = externalFileAttributes;
            return this;
        }

    }

}
