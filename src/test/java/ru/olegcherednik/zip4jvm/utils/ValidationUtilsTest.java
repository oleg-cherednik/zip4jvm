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
