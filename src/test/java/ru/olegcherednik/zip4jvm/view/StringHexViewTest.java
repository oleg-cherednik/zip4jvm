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
import ru.olegcherednik.zip4jvm.model.Charsets;

import java.io.IOException;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * @author Oleg Cherednik
 * @since 17.12.2019
 */
@Test
public class StringHexViewTest {

    public void shouldPrintOneLineWhenLessThanColumnWidth() throws IOException {
        String[] lines = Zip4jvmSuite.execute(new StringHexView("Oleg Cherednik", Charsets.UTF_8, 0, 52));
        assertThat(lines).hasSize(2);
        assertThat(lines[0]).isEqualTo("                                                    UTF-8");
        assertThat(lines[1]).isEqualTo("4F 6C 65 67 20 43 68 65 72 65 64 6E 69 6B           Oleg Cherednik");
    }

    public void shouldPrintMoreThanOneLineWhenMoreThanColumnWidth() throws IOException {
        String[] lines = Zip4jvmSuite.execute(new StringHexView("bikes/honda-cbr600rr.jpg", Charsets.UTF_8, 0, 52));
        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo("                                                    UTF-8");
        assertThat(lines[1]).isEqualTo("62 69 6B 65 73 2F 68 6F 6E 64 61 2D 63 62 72 36 30  bikes/honda-cbr60");
        assertThat(lines[2]).isEqualTo("30 72 72 2E 6A 70 67                                0rr.jpg");
    }

    public void shouldPrintUnicodeStringWhenMoreThanColumnWidth() throws IOException {
        String[] lines = Zip4jvmSuite.execute(new StringHexView("Олег Чередник", Charsets.UTF_8, 0, 52));
        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo("                                                    UTF-8");
        assertThat(lines[1]).isEqualTo("D0 9E D0 BB D0 B5 D0 B3 20 D0 A7 D0 B5 D1 80 D0 B5  Олег Чере");
        assertThat(lines[2]).isEqualTo("D0 B4 D0 BD D0 B8 D0 BA                             дник");
    }

    public void shouldFillWholeColumnWhenColumnWidthNotStandard() throws IOException {
        String[] lines = Zip4jvmSuite.execute(new StringHexView("bikes/honda-cbr600rr.jpg", Charsets.UTF_8, 0, 64));
        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo("                                                                UTF-8");
        assertThat(lines[1]).isEqualTo("62 69 6B 65 73 2F 68 6F 6E 64 61 2D 63 62 72 36 30 30 72 72 2E  bikes/honda-cbr600rr.");
        assertThat(lines[2]).isEqualTo("6A 70 67                                                        jpg");
    }

    public void shouldRetrieveFalseWhenDataEmpty() {
        PrintStream out = mock(PrintStream.class);
        assertThat(new StringHexView(null, Charsets.UTF_8, 4, 52).print(out)).isFalse();
        assertThat(new StringHexView("", Charsets.UTF_8, 4, 52).print(out)).isFalse();
        verifyNoInteractions(out);
    }

    public void shouldPrintDotInsteadOfControlCharacterWhenCommentContainsNotPrintableCharacters() throws IOException {
        String[] lines = Zip4jvmSuite.execute(new StringHexView("Олег\n\n\n", Charsets.UTF_8, 0, 52));
        assertThat(lines).hasSize(2);
        assertThat(lines[0]).isEqualTo("                                                    UTF-8");
        assertThat(lines[1]).isEqualTo("D0 9E D0 BB D0 B5 D0 B3 0A 0A 0A                    Олег...");
    }

    public void shouldThrowIllegalArgumentExceptionWhenCharsetNull() {
        assertThatThrownBy(() -> new StringHexView("oleg", null, 4, 52)).isExactlyInstanceOf(IllegalArgumentException.class);
    }
}
