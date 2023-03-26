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
package ru.olegcherednik.zip4jvm.view;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributesTest;
import ru.olegcherednik.zip4jvm.utils.ReflectionUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT0;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT2;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT4;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT6;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT7;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
@Test
public class ExternalFileAttributesViewTest {

    private static final int WINDOWS_READ_ONLY = BIT0;
    private static final int WINDOWS_SYSTEM = BIT2;
    private static final int WINDOWS_DIRECTORY = BIT4;

    private static final int POSIX_OTHERS_EXECUTE = BIT0;
    private static final int POSIX_OTHERS_READ = BIT2;
    private static final int POSIX_GROUP_WRITE = BIT4;
    private static final int POSIX_OWNER_EXECUTE = BIT6;
    private static final int POSIX_OWNER_READ = BIT0;
    private static final int POSIX_REGULAR_FILE = BIT7;

    public void shouldRetrieveWindowsAttributesWhenWin() throws IOException {
        ExternalFileAttributes externalFileAttributes = new ExternalFileAttributes(
                new byte[] { (byte)(WINDOWS_READ_ONLY | WINDOWS_SYSTEM | WINDOWS_DIRECTORY), 0x0, 0x0, 0x0 });

        String[] lines = Zip4jvmSuite.execute(new ExternalFileAttributesView(externalFileAttributes, 0, 52));

        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo("external file attributes:                           0x00000015");
        assertThat(lines[1]).isEqualTo("  WINDOWS   (0x15):                                 rdo sys dir");
        assertThat(lines[2]).isEqualTo("  POSIX (0x000000):                                 " + ExternalFileAttributes.NONE);
    }

    public void shouldRetrievePosixAttributesWhenMacOrUnix() throws Exception {
        for (String osName : Arrays.asList(ExternalFileAttributes.MAC, ExternalFileAttributes.UNIX)) {
            ExternalFileAttributes externalFileAttributes =
                    ReflectionUtils.invokeConstructor(ExternalFileAttributes.class,
                                                      new Class[] { byte[].class, String.class },
                                                      new byte[] { 0x0, 0x0,
                                                              (byte)(POSIX_OTHERS_EXECUTE | POSIX_OTHERS_READ | POSIX_GROUP_WRITE |
                                                                      POSIX_OWNER_EXECUTE),
                                                              (byte)(POSIX_OWNER_READ | POSIX_REGULAR_FILE) }, osName);

            String[] lines = Zip4jvmSuite.execute(new ExternalFileAttributesView(externalFileAttributes, 0, 52));

            assertThat(lines).hasSize(3);
            assertThat(lines[0]).isEqualTo("external file attributes:                           0x81550000");
            assertThat(lines[1]).isEqualTo("  WINDOWS   (0x00):                                 none");
            assertThat(lines[2]).isEqualTo("  POSIX (0x815500):                                 -r-x-w-r-x");
        }
    }

}
