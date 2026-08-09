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

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 17.02.2025
 */
@Test
public class BaseCharsetProviderTest {

    public void shouldRetrieveUtf8CharsetWhenUseUtf8Provider() {
        assertThat(BaseCharsetProvider.UTF_8.apply(null)).isSameAs(Charsets.UTF_8);
        assertThat(BaseCharsetProvider.UTF_8.apply(Charsets.UTF_8)).isSameAs(Charsets.UTF_8);
        assertThat(BaseCharsetProvider.UTF_8.apply(Charsets.SYSTEM)).isSameAs(Charsets.UTF_8);
    }

    public void shouldRetrieveSystemCharsetWhenUseSystemProvider() {
        assertThat(BaseCharsetProvider.SYSTEM.apply(null)).isSameAs(Charsets.SYSTEM);
        assertThat(BaseCharsetProvider.SYSTEM.apply(Charsets.SYSTEM)).isSameAs(Charsets.SYSTEM);
        assertThat(BaseCharsetProvider.SYSTEM.apply(Charsets.UTF_8)).isSameAs(Charsets.SYSTEM);
    }

}
