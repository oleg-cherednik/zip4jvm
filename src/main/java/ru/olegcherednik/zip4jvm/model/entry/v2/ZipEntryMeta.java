package ru.olegcherednik.zip4jvm.model.entry.v2;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang.StringUtils;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.utils.EmptyInputStream;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
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

    @NonNull
    private final IOSupplier2<InputStream> inputStream;
    @NonNull
    private final String fileName;
    private final long lastModifiedTime;
    private final ExternalFileAttributes externalFileAttributes;

    public static ZipEntryMeta of(@NonNull Path path, @NonNull String fileName) throws IOException {
        fileName = StringUtils.removeEnd(fileName, "/");
        fileName = StringUtils.removeEnd(fileName, "\\");
        fileName = Files.isDirectory(path) ? fileName + '/' : fileName;

        return builder()
                .inputStream(Files.isRegularFile(path) ? () -> new FileInputStream(path.toFile()) : () -> EmptyInputStream.INSTANCE)
                .fileName(fileName)
                .lastModifiedTime(Files.getLastModifiedTime(path).toMillis())
                .externalFileAttributes(ExternalFileAttributes.createOperationBasedDelegate(path)).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    private ZipEntryMeta(Builder builder) {
        fileName = ZipUtils.normalizeFileName(builder.fileName);
        inputStream = ZipUtils.isDirectory(fileName) ? () -> EmptyInputStream.INSTANCE : builder.inputStream;
        lastModifiedTime = builder.lastModifiedTime;
        externalFileAttributes = builder.externalFileAttributes;
    }

    public boolean isDirectory() {
        return ZipUtils.isDirectory(fileName);
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
