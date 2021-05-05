package ru.olegcherednik.zip4jvm.model.settings;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import ru.olegcherednik.zip4jvm.model.Charsets;

import java.nio.charset.Charset;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 06.10.2019
 */
@Getter
public final class UnzipSettings {

    public static final UnzipSettings DEFAULT = builder().build();

    private final Function<String, char[]> passwordProvider;
    private final Function<Charset, Charset> charsetCustomizer;

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return builder().password(passwordProvider).charsetCustomizer(charsetCustomizer);
    }

    private UnzipSettings(Builder builder) {
        passwordProvider = builder.passwordProvider;
        charsetCustomizer = builder.charsetCustomizer;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private static final Function<String, char[]> NO_PASSWORD_PROVIDER = fileName -> null;

        private Function<String, char[]> passwordProvider = NO_PASSWORD_PROVIDER;
        private Function<Charset, Charset> charsetCustomizer = Charsets.UNMODIFIED;

        public UnzipSettings build() {
            return new UnzipSettings(this);
        }

        @SuppressWarnings("MethodCanBeVariableArityMethod")
        public Builder password(char[] password) {
            passwordProvider = ArrayUtils.isEmpty(password) ? NO_PASSWORD_PROVIDER : fileName -> password;
            return this;
        }

        public Builder password(Function<String, char[]> passwordProvider) {
            this.passwordProvider = Optional.ofNullable(passwordProvider).orElse(NO_PASSWORD_PROVIDER);
            return this;
        }

        public Builder charset(Charset charset) {
            charsetCustomizer = charset == null ? Charsets.UNMODIFIED : curCharset -> charset;
            return this;
        }

        private Builder charsetCustomizer(Function<Charset, Charset> charsetCustomizer) {
            this.charsetCustomizer = Optional.ofNullable(charsetCustomizer).orElse(Charsets.UNMODIFIED);
            return this;
        }


    }

}
