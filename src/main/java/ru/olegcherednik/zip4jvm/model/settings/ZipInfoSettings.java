package ru.olegcherednik.zip4jvm.model.settings;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.model.Charsets;

import java.nio.charset.Charset;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 05.12.2019
 */
@Getter
public final class ZipInfoSettings {

    public static final ZipInfoSettings DEFAULT = builder().build();

    private final boolean readEntries;
    private final boolean copyPayload;
    private final Function<Charset, Charset> customizeCharset;
    private final int offs;
    private final int columnWidth;

    public static Builder builder() {
        return new Builder();
    }

    private ZipInfoSettings(Builder builder) {
        readEntries = builder.readEntries;
        copyPayload = builder.copyPayload;
        customizeCharset = builder.customizeCharset;
        offs = builder.offs;
        columnWidth = builder.columnWidth;
    }

    public Charset getCharset() {
        return customizeCharset.apply(Charsets.ZIP_DEFAULT);
    }

    public static final class Builder {

        private boolean readEntries = true;
        private boolean copyPayload;
        private Function<Charset, Charset> customizeCharset = ch -> Charsets.UTF_8;
        private int offs = 4;
        private int columnWidth = 52;

        public ZipInfoSettings build() {
            return new ZipInfoSettings(this);
        }

        public Builder readEntries(boolean readEntries) {
            this.readEntries = readEntries;
            return this;
        }

        public Builder copyPayload(boolean copyPayload) {
            this.copyPayload = copyPayload;
            return this;
        }

        public Builder customizeCharset(Function<Charset, Charset> customizeCharset) {
            this.customizeCharset = Optional.ofNullable(customizeCharset).orElse(Charsets.UNMODIFIED);
            return this;
        }

        public Builder offs(int offs) {
            this.offs = offs;
            return this;
        }

        public Builder columnWidth(int columnWidth) {
            this.columnWidth = columnWidth;
            return this;
        }
    }
}
