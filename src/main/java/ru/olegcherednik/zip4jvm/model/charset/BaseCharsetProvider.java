/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
package ru.olegcherednik.zip4jvm.model.charset;

import lombok.RequiredArgsConstructor;

import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 17.02.2025
 */
@RequiredArgsConstructor
public class BaseCharsetProvider implements CharsetProvider {

    public static final CharsetProvider UTF_8 = new BaseCharsetProvider(Charsets.UTF_8);
    public static final CharsetProvider SYSTEM = new BaseCharsetProvider(Charsets.SYSTEM);

    private final Charset charset;

    // ---------- CharsetProvider ----------

    @Override
    public Charset apply(Charset charset) {
        return this.charset;
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return charset.name();
    }
}

