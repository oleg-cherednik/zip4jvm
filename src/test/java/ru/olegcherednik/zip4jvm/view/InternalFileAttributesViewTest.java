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
package ru.olegcherednik.zip4jvm.view;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.InternalFileAttributes;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
@Test
public class InternalFileAttributesViewTest {

    public void shouldRetrieveWindowsAttributesWhenWin() {
        String[] lines = Zip4jvmSuite.execute(
                new InternalFileAttributesView(new InternalFileAttributes(new byte[] { 0x1, 0x2 }), 0, 52));

        assertThat(lines).hasSize(2);
        assertThat(lines[0]).isEqualTo("internal file attributes:                           0x0201");
        assertThat(lines[1]).isEqualTo("  apparent file type:                               text");
    }

}
