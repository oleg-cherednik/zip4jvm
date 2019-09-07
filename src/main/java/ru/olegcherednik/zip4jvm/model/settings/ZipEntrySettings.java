package ru.olegcherednik.zip4jvm.model.settings;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

@Getter
public final class ZipEntrySettings {

    private final Compression compression;
    private final CompressionLevel compressionLevel;
    private final Encryption encryption;
    private final char[] password;
    private final String comment;
    private final boolean zip64;
    private final boolean utf8;
    private String basePath = "";

    public static Builder builder() {
        return new Builder();
    }

    private ZipEntrySettings(Builder builder) {
        compression = builder.compression;
        compressionLevel = builder.compressionLevel;
        encryption = builder.encryption;
        password = builder.password;
        comment = builder.comment;
        zip64 = builder.zip64;
        utf8 = builder.utf8;
        basePath = builder.basePath;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private Compression compression = Compression.DEFLATE;
        private CompressionLevel compressionLevel = CompressionLevel.NORMAL;
        private Encryption encryption = Encryption.OFF;
        private char[] password;
        private String comment;
        private boolean zip64;
        private boolean utf8 = true;
        private String basePath = "";

        public ZipEntrySettings build() {
            return new ZipEntrySettings(this);
        }

        public ZipEntrySettings.Builder compression(@NonNull Compression compression, @NonNull CompressionLevel compressionLevel) {
            this.compression = compression;
            this.compressionLevel = compressionLevel;
            return this;
        }

        @SuppressWarnings("MethodCanBeVariableArityMethod")
        public ZipEntrySettings.Builder encryption(@NonNull Encryption encryption, @NonNull char[] password) {
            if (encryption != Encryption.OFF) {
                this.encryption = encryption;
                this.password = ArrayUtils.clone(password);
            }

            return this;
        }

        public ZipEntrySettings.Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public ZipEntrySettings.Builder zip64(boolean zip64) {
            this.zip64 = zip64;
            return this;
        }

        public ZipEntrySettings.Builder utf8(boolean utf8) {
            this.utf8 = utf8;
            return this;
        }

        public ZipEntrySettings.Builder basePath(@NonNull String basePath) {
            basePath = StringUtils.trimToEmpty(ZipUtils.normalizeFileName(basePath));
            this.basePath = basePath.startsWith("/") ? basePath.substring(1) : basePath;
            return this;
        }
    }
}
