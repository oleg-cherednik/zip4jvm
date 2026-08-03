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
package ru.olegcherednik.zip4jvm.utils;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * @author Oleg Cherednik
 * @since 14.04.2025
 */
@Test
public class ValidationUtilsTest {

    public void shouldRetrieveTrueWhenCheckForValidEntryName() {
        assertThatCode(() -> ValidationUtils.requireValidEntryName("aaa.jpg")).doesNotThrowAnyException();
        assertThatCode(() -> ValidationUtils.requireValidEntryName("..aaa.jpg")).doesNotThrowAnyException();
        assertThatCode(() -> ValidationUtils.requireValidEntryName("aa/aaa.jpg")).doesNotThrowAnyException();
        assertThatCode(() -> ValidationUtils.requireValidEntryName("aa/..aaa.jpg")).doesNotThrowAnyException();
        assertThatCode(() -> ValidationUtils.requireValidEntryName("aa\\aaa.jpg")).doesNotThrowAnyException();
        assertThatCode(() -> ValidationUtils.requireValidEntryName("/aa/aaa.jpg")).doesNotThrowAnyException();
        assertThatCode(() -> ValidationUtils.requireValidEntryName("\\aa/aaa.jpg")).doesNotThrowAnyException();
    }

    public void shouldRetrieveFalseWhenCheckForNotValidEntryName() {
        assertThatCode(() -> ValidationUtils.requireValidEntryName("../aaa.jpg"))
                .isExactlyInstanceOf(IllegalArgumentException.class);
        assertThatCode(() -> ValidationUtils.requireValidEntryName("/../aaa.jpg"))
                .isExactlyInstanceOf(IllegalArgumentException.class);
        assertThatCode(() -> ValidationUtils.requireValidEntryName("\\../aaa.jpg"))
                .isExactlyInstanceOf(IllegalArgumentException.class);
        assertThatCode(() -> ValidationUtils.requireValidEntryName("\\../aaa.jpg"))
                .isExactlyInstanceOf(IllegalArgumentException.class);
        assertThatCode(() -> ValidationUtils.requireValidEntryName(".."))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

}
