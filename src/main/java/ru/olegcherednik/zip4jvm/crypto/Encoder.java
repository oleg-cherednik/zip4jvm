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
package ru.olegcherednik.zip4jvm.crypto;

import ru.olegcherednik.zip4jvm.io.out.DataOutput;

/**
 * {@link Encoder} gives an ability to write encrypted data to the output
 * resource represented as {@link DataOutput}
 *
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public interface Encoder {

    Encoder NULL = new NullEncoder();

    /**
     * Encrypt the given byte {@code b} and write result to {@code out}. In one
     * invoke zero or multiple bytes can be written to {@code out}.
     */
    void encrypt(byte b, DataOutput out);

    /**
     * Write encryption header stored in {@link Encoder} to {@code out}. This
     * is optional and depending on encoder implementation.
     */
    void writeEncryptionHeaderWhenRequired(DataOutput out);

    default void close(DataOutput out) {
        /* nothing to close */
    }

}
