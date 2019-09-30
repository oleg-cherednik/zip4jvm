package ru.olegcherednik.zip4jvm.model;

import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * @author Oleg Cherednik
 * @since 25.09.2019
 */
@Test
public class InternalFileAttributesTest {

    public void shouldRetrieveNullWhenDataEmpty() {
        assertThat(InternalFileAttributes.build(null)).isSameAs(InternalFileAttributes.NULL);
        assertThat(InternalFileAttributes.build(new byte[] { 0xA })).isSameAs(InternalFileAttributes.NULL);
        assertThat(InternalFileAttributes.build(new byte[] { 0xA, 0xA, 0xA })).isSameAs(InternalFileAttributes.NULL);
        assertThat(InternalFileAttributes.build(new byte[] { 0x0, 0x0 })).isSameAs(InternalFileAttributes.NULL);
    }

    public void shouldRetrieveNotNullWhenDataNotEmpty() {
        for (byte[] data : Arrays.asList(new byte[] { 0x0, 0xA }, new byte[] { 0xA, 0x0 }, new byte[] { 0xA, 0xA })) {
            InternalFileAttributes attributes = InternalFileAttributes.build(data);
            assertThat(attributes).isNotNull();
            assertThat(attributes).isNotSameAs(InternalFileAttributes.NULL);
            assertThat(attributes.toString()).isEqualTo("internal");
        }
    }

    public void shouldRetrieveNullWhenToStringForNullObject() {
        assertThat(InternalFileAttributes.NULL.toString()).isEqualTo("<null>");
    }

    public void shouldNotThrowExceptionWhenAcceptPath() {
        Path path = Paths.get("foo");
        assertThatCode(() -> InternalFileAttributes.build(new byte[] { 0xA, 0xA }).accept(path)).doesNotThrowAnyException();
        assertThatCode(() -> InternalFileAttributes.NULL.accept(path)).doesNotThrowAnyException();
    }

}
