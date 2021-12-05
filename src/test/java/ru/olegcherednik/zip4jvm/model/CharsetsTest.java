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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 04.01.2020
 */
@Test
public class CharsetsTest {

    public void shouldRetrieveSystemCharsetWhenUseSystemCharsetFunction() {
        assertThat(Charsets.SYSTEM_CHARSET.apply(null)).isSameAs(Charsets.SYSTEM);
        assertThat(Charsets.SYSTEM_CHARSET.apply(Charsets.SYSTEM)).isSameAs(Charsets.SYSTEM);
        assertThat(Charsets.SYSTEM_CHARSET.apply(Charsets.UTF_8)).isSameAs(Charsets.SYSTEM);
    }

    public void shouldRetrieveUnmodifiedCharsetWhenUseUnmodifiedFunction() {
        assertThat(Charsets.UNMODIFIED.apply(null)).isNull();
        assertThat(Charsets.UNMODIFIED.apply(Charsets.UTF_8)).isSameAs(Charsets.UTF_8);
        assertThat(Charsets.UNMODIFIED.apply(Charsets.SYSTEM)).isSameAs(Charsets.SYSTEM);
    }
}
