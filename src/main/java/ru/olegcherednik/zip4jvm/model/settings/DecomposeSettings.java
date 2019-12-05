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
public final class DecomposeSettings {

    private final Function<Charset, Charset> customizeCharset;
    private final int offs;
    private final int columnWidth;

    public static Builder builder() {
        return new Builder();
    }

    private DecomposeSettings(Builder builder) {
        customizeCharset = builder.customizeCharset;
        offs = builder.offs;
        columnWidth = builder.columnWidth;
    }

    public Charset getCharset() {
        return customizeCharset.apply(Charsets.ZIP_DEFAULT);
    }

    public static final class Builder {

        private Function<Charset, Charset> customizeCharset = Charsets.UNMODIFIED;
        private int offs;
        private int columnWidth;

        public DecomposeSettings build() {
            return new DecomposeSettings(this);
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
