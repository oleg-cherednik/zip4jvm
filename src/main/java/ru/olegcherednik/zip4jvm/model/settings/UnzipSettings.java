/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.model.settings;

import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.password.NoPasswordProvider;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;
import ru.olegcherednik.zip4jvm.model.password.SinglePasswordProvider;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 06.10.2019
 */
@Getter
public final class UnzipSettings {

    public static final UnzipSettings DEFAULT = builder().build();

    public static final int ASYNC_THREADS_OFF = 0;
    public static final int ASYNC_THREADS_AUTO = -1;

    private final PasswordProvider passwordProvider;
    private final Function<Charset, Charset> charsetCustomizer;
    private final int asyncThreads;

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return builder()
                .passwordProvider(passwordProvider)
                .charsetCustomizer(charsetCustomizer)
                .asyncThreads(asyncThreads);
    }

    private UnzipSettings(Builder builder) {
        passwordProvider = builder.passwordProvider;
        charsetCustomizer = builder.charsetCustomizer;
        asyncThreads = builder.asyncThreads;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
    public static final class Builder {

        private PasswordProvider passwordProvider = NoPasswordProvider.INSTANCE;
        private Function<Charset, Charset> charsetCustomizer = Charsets.UNMODIFIED;

        private int asyncThreads = ASYNC_THREADS_AUTO;

        public UnzipSettings build() {
            return new UnzipSettings(this);
        }

        public Builder password(char[] password) {
            if (ArrayUtils.isEmpty(password))
                passwordProvider = NoPasswordProvider.INSTANCE;
            else {
                passwordProvider = new SinglePasswordProvider(Arrays.copyOf(password, password.length));
            }
            return this;
        }

        public Builder passwordProvider(PasswordProvider passwordProvider) {
            this.passwordProvider = Optional.ofNullable(passwordProvider).orElse(NoPasswordProvider.INSTANCE);
            return this;
        }

        public Builder charset(Charset charset) {
            charsetCustomizer = charset == null ? Charsets.UNMODIFIED : curCharset -> charset;
            return this;
        }

        public Builder async() {
            asyncThreadsAuto();
            return this;
        }

        public Builder asyncThreads(int asyncThreads) {
            this.asyncThreads = Math.max(1, asyncThreads);
            return this;
        }

        public Builder asyncThreadsAuto() {
            asyncThreads = ASYNC_THREADS_AUTO;
            return this;
        }

        public Builder asyncOff() {
            asyncThreads = ASYNC_THREADS_OFF;
            return this;
        }

        private Builder charsetCustomizer(Function<Charset, Charset> charsetCustomizer) {
            this.charsetCustomizer = Optional.ofNullable(charsetCustomizer).orElse(Charsets.UNMODIFIED);
            return this;
        }

    }

}
