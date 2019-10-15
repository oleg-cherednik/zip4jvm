package ru.olegcherednik.zip4jvm.model;

import org.testng.annotations.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 25.09.2019
 */
@Test
public class InternalFileAttributesTest {

//    public void shouldRetrieveNullWhenDataEmpty() {
//        assertThat(InternalFileAttributes.build(null)).isSameAs(InternalFileAttributes.NULL);
//        assertThat(InternalFileAttributes.build(new byte[] { 0xA })).isSameAs(InternalFileAttributes.NULL);
//        assertThat(InternalFileAttributes.build(new byte[] { 0xA, 0xA, 0xA })).isSameAs(InternalFileAttributes.NULL);
//        assertThat(InternalFileAttributes.build(new byte[] { 0x0, 0x0 })).isSameAs(InternalFileAttributes.NULL);
//    }

    public void shouldRetrieveNotNullWhenDataNotEmpty() {
        for (byte[] data : Arrays.asList(new byte[] { 0x0, 0xA }, new byte[] { 0xA, 0x0 }, new byte[] { 0xA, 0xA })) {
            InternalFileAttributes attributes = InternalFileAttributes.build(data);
            assertThat(attributes).isNotNull();
// TODO temporary
//            assertThat(attributes).isNotSameAs(InternalFileAttributes.NULL);
            assertThat(attributes.toString()).isEqualTo("internal");
        }
    }

// TODO temporary
//    public void shouldRetrieveNullWhenToStringForNullObject() {
//        assertThat(InternalFileAttributes.NULL.toString()).isEqualTo("<null>");
//    }

}
