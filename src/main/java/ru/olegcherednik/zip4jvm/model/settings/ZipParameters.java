package ru.olegcherednik.zip4jvm.model.settings;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

import java.nio.file.Files;
import java.nio.file.Path;

@Getter
public final class ZipParameters {

    private final Compression compression;
    private final CompressionLevel compressionLevel;
    private final Encryption encryption;
    private final char[] password;
    private final long splitLength;
    private final String comment;
    /**
     * Write all entries as well as entire zip archive in ZIP64 format.
     * If it's {@literal false}, then it will be automatically set if require.
     */
    private final boolean zip64;
    private final boolean utf8;

    @Setter
    private Path defaultFolderPath;

    public static Builder builder() {
        return new Builder();
    }

    private ZipParameters(Builder builder) {
        compression = builder.compression;
        compressionLevel = builder.compressionLevel;
        encryption = builder.encryption;
        password = builder.password;
        splitLength = builder.splitLength;
        comment = builder.comment;
        zip64 = builder.zip64;
        utf8 = builder.utf8;
        defaultFolderPath = builder.defaultFolderPath;
    }

    public String getRelativeFileName(Path path) {
        path = path.toAbsolutePath();
        Path root = defaultFolderPath != null ? defaultFolderPath : path.getParent();
        String str = root.relativize(path).toString();

        if (Files.isDirectory(path))
            str += '/';

        return ZipUtils.normalizeFileName(str);
    }

    public static final class Builder {

        private Compression compression = Compression.DEFLATE;
        private CompressionLevel compressionLevel = CompressionLevel.NORMAL;
        private Encryption encryption = Encryption.OFF;
        private char[] password;
        private long splitLength = ZipModel.NO_SPLIT;
        private String comment;
        private boolean zip64;
        private boolean utf8 = true;
        private Path defaultFolderPath;

        public ZipParameters build() {
            return new ZipParameters(this);
        }

        public Builder compression(@NonNull Compression compression, @NonNull CompressionLevel compressionLevel) {
            this.compression = compression;
            this.compressionLevel = compressionLevel;
            return this;
        }

        @SuppressWarnings("MethodCanBeVariableArityMethod")
        public Builder encryption(@NonNull Encryption encryption, @NonNull char[] password) {
            if (encryption != Encryption.OFF) {
                this.encryption = encryption;
                this.password = ArrayUtils.clone(password);
            }

            return this;
        }

        public Builder splitLength(long splitLength) {
            this.splitLength = splitLength <= 0 ? ZipModel.NO_SPLIT : splitLength;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder zip64(boolean zip64) {
            this.zip64 = zip64;
            return this;
        }

        public Builder utf8(boolean utf8) {
            this.utf8 = utf8;
            return this;
        }

        public Builder defaultFolderPath(Path defaultFolderPath) {
            this.defaultFolderPath = defaultFolderPath;
            return this;
        }
    }
}
