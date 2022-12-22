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
package ru.olegcherednik.zip4jvm.model;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.exception.CompressionNotSupportedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Oleg Cherednik
 * @since 28.09.2019
 */
@Test
public class CompressionTest {

    public void shouldParseCompressionMethodWhenSupportedMethod() {
        assertThat(Compression.parseCompressionMethod(CompressionMethod.STORE)).isSameAs(Compression.STORE);
        assertThat(Compression.parseCompressionMethod(CompressionMethod.DEFLATE)).isSameAs(Compression.DEFLATE);
    }

    public void shouldThrowExceptionWhenCompressionMethodNotSupported() {
        for (CompressionMethod compressionMethod : CompressionMethod.values())
            if (parseCompressionMethod(compressionMethod) == null)
                assertThatThrownBy(() -> Compression.parseCompressionMethod(compressionMethod))
                        .isExactlyInstanceOf(CompressionNotSupportedException.class);
    }

    private static Compression parseCompressionMethod(CompressionMethod compressionMethod) {
        try {
            return Compression.parseCompressionMethod(compressionMethod);
        } catch(CompressionNotSupportedException ignore) {
            return null;
        }
    }

}
